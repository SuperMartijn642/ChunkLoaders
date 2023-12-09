package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.ChunkLoadersConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created 23/02/2022 by SuperMartijn642
 */
public class PlayerActivityTracker {

    public static void registerCallbacks(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLeave(handler.getPlayer()));
        ServerTickEvents.START_SERVER_TICK.register(PlayerActivityTracker::onServerTick);
        ServerLifecycleEvents.SERVER_STARTING.register(PlayerActivityTracker::onServerStarting);
    }

    /**
     * Store all players who are currently online or who have been online within the inactivity timeout period
     */
    private static final Set<UUID> activePlayers = new LinkedHashSet<>();
    /**
     * Store all players who are currently online
     */
    private static final Set<UUID> onlinePlayers = new LinkedHashSet<>();
    /**
     * Store the last activity time for players still within the inactivity timeout period
     */
    private static final Map<UUID,ActiveTime> lastActiveTimePerPlayer = new HashMap<>();
    /**
     * Sort the last activity time for players still within the inactivity timeout period
     */
    private static final SortedSet<ActiveTime> sortedActiveTimes = new TreeSet<>();

    private static boolean dirty = false;

    public static void onPlayerJoin(Player player){
        UUID playerId = player.getUUID();
        onlinePlayers.add(playerId);
        if(!activePlayers.contains(playerId)){
            activePlayers.add(playerId);
            if(isInactivityTimeOutEnabled())
                player.getServer().getAllLevels().forEach(level -> ChunkLoadingCapability.get(level).castServer().togglePlayerActivity(playerId, true));
        }
        ActiveTime lastActiveTime = lastActiveTimePerPlayer.remove(playerId);
        if(lastActiveTime != null)
            sortedActiveTimes.remove(lastActiveTime);
        dirty = true;
    }

    public static void onPlayerLeave(Player player){
        UUID playerId = player.getUUID();
        onlinePlayers.remove(playerId);
        ActiveTime lastActiveTime = new ActiveTime(playerId, System.currentTimeMillis());
        lastActiveTimePerPlayer.put(playerId, lastActiveTime);
        sortedActiveTimes.add(lastActiveTime);
        dirty = true;
    }

    public static void onServerTick(MinecraftServer server){
        long timeoutTime = System.currentTimeMillis() - getInactivityTimeout();
        // Remove players who have timed out
        ActiveTime earliestExpiringTime;
        while(!sortedActiveTimes.isEmpty() && (earliestExpiringTime = sortedActiveTimes.first()).lastActiveTime < timeoutTime){
            sortedActiveTimes.remove(earliestExpiringTime);
            lastActiveTimePerPlayer.remove(earliestExpiringTime.player);
            activePlayers.remove(earliestExpiringTime.player);
            dirty = true;
            final ActiveTime finalEarliestExpiringTime = earliestExpiringTime;
            if(isInactivityTimeOutEnabled())
                server.getAllLevels().forEach(level -> ChunkLoadingCapability.get(level).castServer().togglePlayerActivity(finalEarliestExpiringTime.player, false));
        }
    }

    public static void onServerStarting(MinecraftServer server){
        // Clear all values when switching between saves
        activePlayers.clear();
        onlinePlayers.clear();
        lastActiveTimePerPlayer.clear();
        sortedActiveTimes.clear();
        dirty = false;

        // Load the data from the world folder
        Path path = server.getWorldPath(LevelResource.ROOT).resolve("chunkloaders/active_players.nbt");
        if(!Files.exists(path))
            return;
        try{
            CompoundTag data = NbtIo.read(path);
            if(data != null)
                read(data);
        }catch(IOException exception){
            ChunkLoaders.LOGGER.error("Failed to load player activity data!", exception);
        }
    }

    public static void onWorldSave(ServerLevel level){
        // Save everything when world gets saved
        if(dirty){
            CompoundTag data = write();
            Path path = level.getServer().getWorldPath(LevelResource.ROOT).resolve("chunkloaders/active_players.nbt");
            try{
                Files.createDirectories(path.getParent());
                NbtIo.write(data, path);
            }catch(IOException exception){
                ChunkLoaders.LOGGER.error("Failed to write active player data!", exception);
                return;
            }
            dirty = false;
        }
    }

    public static boolean isPlayerActive(UUID player){
        return !isInactivityTimeOutEnabled() || activePlayers.contains(player);
    }

    private static boolean isInactivityTimeOutEnabled(){
        return ChunkLoadersConfig.inactivityTimeout.get() > 0;
    }

    private static long getInactivityTimeout(){
        // Convert from hours to milliseconds
        return ChunkLoadersConfig.inactivityTimeout.get() * 60 * 1000;
    }

    private static CompoundTag write(){
        ListTag activeTimes = new ListTag();

        // Write currently active players
        for(UUID player : onlinePlayers){
            CompoundTag tag = new CompoundTag();
            tag.putUUID("player", player);
            tag.putLong("time", System.currentTimeMillis());
            activeTimes.add(tag);
        }

        // Write previously online players
        for(ActiveTime activeTime : lastActiveTimePerPlayer.values()){
            CompoundTag tag = new CompoundTag();
            tag.putUUID("player", activeTime.player);
            tag.putLong("time", activeTime.lastActiveTime);
            activeTimes.add(tag);
        }

        // Write the list to a tag
        CompoundTag tag = new CompoundTag();
        tag.put("times", activeTimes);

        return tag;
    }

    private static void read(CompoundTag tag){
        // Get the list from the tag
        ListTag activeTimes = tag.getList("times", Tag.TAG_COMPOUND);
        for(Tag nbt : activeTimes){
            if(!(nbt instanceof CompoundTag))
                continue;

            CompoundTag timeTag = (CompoundTag)nbt;
            if(!timeTag.contains("player", Tag.TAG_INT_ARRAY) || !timeTag.contains("time", Tag.TAG_LONG))
                continue;

            ActiveTime activeTime = new ActiveTime(timeTag.getUUID("player"), timeTag.getLong("time"));
            activePlayers.add(activeTime.player);
            sortedActiveTimes.add(activeTime);
        }
    }

    private static class ActiveTime implements Comparable<ActiveTime> {

        public final UUID player;
        public long lastActiveTime;

        public ActiveTime(UUID player, long lastActiveTime){
            this.player = player;
            this.lastActiveTime = lastActiveTime;
        }

        @Override
        public int compareTo(ActiveTime other){
            return Long.compare(this.lastActiveTime, other.lastActiveTime);
        }
    }
}
