package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.ChunkLoadersConfig;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
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
        UUID playerId = e.getPlayer().getUUID();
        onlinePlayers.add(playerId);
        if(!activePlayers.contains(playerId)){
            activePlayers.add(playerId);
            if(isInactivityTimeOutEnabled())
                e.getPlayer().getServer().getAllLevels().forEach(level -> ChunkLoadingCapability.get(level).castServer().togglePlayerActivity(playerId, true));
        }
        ActiveTime lastActiveTime = lastActiveTimePerPlayer.remove(playerId);
        if(lastActiveTime != null)
            sortedActiveTimes.remove(lastActiveTime);
        dirty = true;
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent e){
        UUID playerId = e.getPlayer().getUUID();
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
    public static void onServerStarting(FMLServerStartingEvent e){
        // Clear all values when switching between saves
        activePlayers.clear();
        onlinePlayers.clear();
        lastActiveTimePerPlayer.clear();
        sortedActiveTimes.clear();
        dirty = false;
    }

    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save e){
        if(!(e.getWorld() instanceof ServerWorld))
            return;

        // Save everything when world gets saved
        if(dirty){
            CompoundNBT data = write();
            File file = new File(((ServerWorld)e.getWorld()).getServer().getWorldPath(FolderName.ROOT).toFile(), "chunkloaders/active_players.nbt");
            file.getParentFile().mkdirs();
            try{
                CompressedStreamTools.write(data, file);
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

    private static CompoundNBT write(){
        ListNBT activeTimes = new ListNBT();

        // Write currently active players
        for(UUID player : onlinePlayers){
            CompoundNBT tag = new CompoundNBT();
            tag.putUUID("player", player);
            tag.putLong("time", System.currentTimeMillis());
            activeTimes.add(tag);
        }

        // Write previously online players
        for(ActiveTime activeTime : lastActiveTimePerPlayer.values()){
            CompoundNBT tag = new CompoundNBT();
            tag.putUUID("player", activeTime.player);
            tag.putLong("time", activeTime.lastActiveTime);
            activeTimes.add(tag);
        }

        // Write the list to a tag
        CompoundNBT tag = new CompoundNBT();
        tag.put("times", activeTimes);

        return tag;
    }

    private static void read(CompoundNBT tag){
        // Get the list from the tag
        ListNBT activeTimes = tag.getList("times", Constants.NBT.TAG_COMPOUND);
        for(INBT nbt : activeTimes){
            if(!(nbt instanceof CompoundNBT))
                continue;

            CompoundNBT timeTag = (CompoundNBT)nbt;
            if(!timeTag.contains("player", Constants.NBT.TAG_INT_ARRAY) || !timeTag.contains("time", Constants.NBT.TAG_LONG))
                continue;

            ActiveTime activeTime = new ActiveTime(tag.getUUID("player"), tag.getLong("time"));
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
