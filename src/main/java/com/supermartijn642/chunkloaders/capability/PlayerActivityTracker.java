package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.ChunkLoadersConfig;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
        UUID playerId = e.player.getUniqueID();
        onlinePlayers.add(playerId);
        if(!activePlayers.contains(playerId)){
            activePlayers.add(playerId);
            if(isInactivityTimeOutEnabled())
                Arrays.stream(DimensionManager.getWorlds()).forEach(level -> ChunkLoadingCapability.get(level).castServer().togglePlayerActivity(playerId, true));
        }
        ActiveTime lastActiveTime = lastActiveTimePerPlayer.remove(playerId);
        if(lastActiveTime != null)
            sortedActiveTimes.remove(lastActiveTime);
        dirty = true;
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent e){
        UUID playerId = e.player.getUniqueID();
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
                Arrays.stream(DimensionManager.getWorlds()).forEach(level -> ChunkLoadingCapability.get(level).castServer().togglePlayerActivity(finalEarliestExpiringTime.player, false));
        }
    }

    public static void onServerStarting(FMLServerAboutToStartEvent e){
        // Clear all values when switching between saves
        activePlayers.clear();
        onlinePlayers.clear();
        lastActiveTimePerPlayer.clear();
        sortedActiveTimes.clear();
        dirty = false;

        // Load the data from the world folder
        File file = e.getServer().getActiveAnvilConverter().getFile(e.getServer().getFolderName(), "chunkloaders/active_players.nbt");
        if(!file.exists())
            return;
        try{
            NBTTagCompound data = CompressedStreamTools.read(file);
            if(data != null)
                read(data);
        }catch(IOException exception){
            ChunkLoaders.LOGGER.error("Failed to load player activity data!", exception);
        }
    }

    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save e){
        if(!(e.getWorld() instanceof WorldServer))
            return;

        // Save everything when world gets saved
        if(dirty){
            NBTTagCompound data = write();
            File file = e.getWorld().getMinecraftServer().getActiveAnvilConverter().getFile(e.getWorld().getMinecraftServer().getFolderName(), "chunkloaders/active_players.nbt");
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

    private static NBTTagCompound write(){
        NBTTagList activeTimes = new NBTTagList();

        // Write currently active players
        for(UUID player : onlinePlayers){
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("player", player);
            tag.setLong("time", System.currentTimeMillis());
            activeTimes.appendTag(tag);
        }

        // Write previously online players
        for(ActiveTime activeTime : lastActiveTimePerPlayer.values()){
            NBTTagCompound tag = new NBTTagCompound();
            tag.setUniqueId("player", activeTime.player);
            tag.setLong("time", activeTime.lastActiveTime);
            activeTimes.appendTag(tag);
        }

        // Write the list to a tag
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("times", activeTimes);

        return tag;
    }

    private static void read(NBTTagCompound tag){
        // Get the list from the tag
        NBTTagList activeTimes = tag.getTagList("times", Constants.NBT.TAG_COMPOUND);
        for(NBTBase nbt : activeTimes){
            if(!(nbt instanceof NBTTagCompound))
                continue;

            NBTTagCompound timeTag = (NBTTagCompound)nbt;
            if(!timeTag.hasKey("player", Constants.NBT.TAG_INT_ARRAY) || !timeTag.hasKey("time", Constants.NBT.TAG_LONG))
                continue;

            ActiveTime activeTime = new ActiveTime(tag.getUniqueId("player"), tag.getLong("time"));
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
