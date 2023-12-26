package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.ChunkLoadersConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created 23/02/2022 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class PlayerActivityTracker {

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

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
        UUID playerId = e.getEntity().getUUID();
        onlinePlayers.add(playerId);
        if(!activePlayers.contains(playerId)){
            activePlayers.add(playerId);
            if(isInactivityTimeOutEnabled())
                e.getEntity().getServer().getAllLevels().forEach(level -> ChunkLoadingCapability.get(level).castServer().togglePlayerActivity(playerId, true));
        }
        ActiveTime lastActiveTime = lastActiveTimePerPlayer.remove(playerId);
        if(lastActiveTime != null)
            sortedActiveTimes.remove(lastActiveTime);
        dirty = true;
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent e){
        UUID playerId = e.getEntity().getUUID();
        onlinePlayers.remove(playerId);
        ActiveTime lastActiveTime = new ActiveTime(playerId, System.currentTimeMillis());
        lastActiveTimePerPlayer.put(playerId, lastActiveTime);
        sortedActiveTimes.add(lastActiveTime);
        dirty = true;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e){
        if(e.phase == TickEvent.Phase.END)
            return;

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
                ServerLifecycleHooks.getCurrentServer().getAllLevels().forEach(level -> ChunkLoadingCapability.get(level).castServer().togglePlayerActivity(finalEarliestExpiringTime.player, false));
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent e){
        // Clear all values when switching between saves
        activePlayers.clear();
        onlinePlayers.clear();
        lastActiveTimePerPlayer.clear();
        sortedActiveTimes.clear();
        dirty = false;

        // Load the data from the world folder
        Path path = e.getServer().getWorldPath(LevelResource.ROOT).resolve("chunkloaders/active_players.nbt");
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

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save e){
        if(!(e.getLevel() instanceof ServerLevel))
            return;

        // Save everything when world gets saved
        if(dirty){
            CompoundTag data = write();
            Path path = e.getLevel().getServer().getWorldPath(LevelResource.ROOT).resolve("chunkloaders/active_players.nbt");
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
