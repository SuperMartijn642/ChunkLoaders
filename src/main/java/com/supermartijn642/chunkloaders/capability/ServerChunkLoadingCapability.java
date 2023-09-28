package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaderBlockEntity;
import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.*;
import com.supermartijn642.core.network.BasePacket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.*;

/**
 * Created 22/02/2022 by SuperMartijn642
 */
public class ServerChunkLoadingCapability extends ChunkLoadingCapability {

    private ForgeChunkManager.Ticket ticket;

    public ServerChunkLoadingCapability(World level){
        super(level);
    }

    public void addChunkLoader(ChunkLoaderBlockEntity entity){
        BlockPos pos = entity.getPos();
        UUID owner = entity.getOwner();
        if(!this.chunkLoadersPerPlayer.containsKey(owner) || !this.chunkLoadersPerPlayer.get(owner).contains(pos)){
            ChunkPos chunkPos = new ChunkPos(pos);
            this.chunkLoadersPerChunk.putIfAbsent(chunkPos, new HashSet<>());
            this.chunkLoadersPerChunk.get(chunkPos).add(pos);
            this.chunkLoadersPerPlayer.putIfAbsent(owner, new HashSet<>());
            this.chunkLoadersPerPlayer.get(owner).add(pos);
            this.chunkLoaderCacheMap.put(pos, new ChunkLoaderCache(pos, entity.getChunkLoaderType(), owner));

            int centerChunkX = entity.getPos().getX() >> 4, centerChunkZ = entity.getPos().getZ() >> 4;
            int range = entity.getChunkLoaderType().getRange();
            this.availableChunksPerPlayer.putIfAbsent(owner, new HashSet<>());
            for(int x = -range + 1; x < range; x++){
                for(int z = -range + 1; z < range; z++){
                    this.availableChunksPerPlayer.get(owner).add(new ChunkPos(centerChunkX + x, centerChunkZ + z));
                }
            }

            this.sendToAllPlayers(new PackedChunkLoaderAdded(pos, owner, entity.getChunkLoaderType()));
        }
    }

    public void removeChunkLoader(ChunkLoaderBlockEntity entity){
        BlockPos pos = entity.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> chunkLoadersPerChunk = this.chunkLoadersPerChunk.get(chunkPos);
        if(chunkLoadersPerChunk != null && chunkLoadersPerChunk.contains(pos)){
            chunkLoadersPerChunk.remove(pos);
            if(chunkLoadersPerChunk.isEmpty())
                this.chunkLoadersPerChunk.remove(chunkPos);
            UUID owner = entity.getOwner();
            Set<BlockPos> chunkLoadersPerPlayer = this.chunkLoadersPerPlayer.get(owner);
            chunkLoadersPerPlayer.remove(pos);
            if(chunkLoadersPerPlayer.isEmpty())
                this.chunkLoadersPerPlayer.remove(owner);
            this.chunkLoaderCacheMap.remove(pos);

            Set<ChunkPos> loadedChunksPerPlayer = this.loadedChunksPerPlayer.get(owner);
            Set<ChunkPos> availableChunksPerPlayer = this.availableChunksPerPlayer.get(owner);
            int radius = entity.getChunkLoaderType().getRange();
            for(int x = -radius + 1; x < radius; x++){
                loop:
                for(int z = -radius + 1; z < radius; z++){
                    ChunkPos otherChunk = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                    if(this.chunkLoadersPerPlayer.containsKey(owner)){
                        for(BlockPos chunkLoaderPos : this.chunkLoadersPerPlayer.get(owner)){
                            int chunkLoaderChunkX = chunkLoaderPos.getX() >> 4, chunkLoaderChunkZ = chunkLoaderPos.getZ() >> 4;
                            ChunkLoaderType type = this.chunkLoaderCacheMap.get(chunkLoaderPos).chunkLoaderType;
                            if(Math.abs(chunkLoaderChunkX - otherChunk.x) < type.getRange() && Math.abs(chunkLoaderChunkZ - otherChunk.z) < type.getRange())
                                continue loop;
                        }
                    }

                    if(loadedChunksPerPlayer != null && loadedChunksPerPlayer.contains(otherChunk))
                        this.stopLoadingChunk(owner, otherChunk);
                    availableChunksPerPlayer.remove(otherChunk);
                }
            }
            if(availableChunksPerPlayer.isEmpty())
                this.availableChunksPerPlayer.remove(owner);

            this.sendToAllPlayers(new PackedChunkLoaderRemoved(pos, owner, entity.getChunkLoaderType()));
        }
    }

    public void startLoadingChunk(UUID player, ChunkPos chunkPos){
        if(!this.canPlayerLoadChunk(player, chunkPos))
            return;

        boolean active = PlayerActivityTracker.isPlayerActive(player);
        if(active){
            this.activePlayersPerLoadedChunk.putIfAbsent(chunkPos, new HashSet<>());
            this.activePlayersPerLoadedChunk.get(chunkPos).add(player);
        }else{
            this.inactivePlayersPerLoadedChunk.putIfAbsent(chunkPos, new HashSet<>());
            this.inactivePlayersPerLoadedChunk.get(chunkPos).add(player);
        }
        this.loadedChunksPerPlayer.putIfAbsent(player, new HashSet<>());
        this.loadedChunksPerPlayer.get(player).add(chunkPos);

        this.loadChunk(chunkPos);

        this.sendToAllPlayers(new PackedStartLoadingChunk(player, chunkPos, active));
    }

    public void stopLoadingChunk(UUID player, ChunkPos chunkPos){
        Set<UUID> inactivePlayer = this.inactivePlayersPerLoadedChunk.get(chunkPos);
        if(inactivePlayer != null){
            inactivePlayer.remove(player);
            if(inactivePlayer.isEmpty())
                this.inactivePlayersPerLoadedChunk.remove(chunkPos);
        }
        Set<UUID> players = this.activePlayersPerLoadedChunk.get(chunkPos);
        if(players != null){
            players.remove(player);
            if(players.isEmpty())
                this.activePlayersPerLoadedChunk.remove(chunkPos);
        }
        if((inactivePlayer == null || inactivePlayer.isEmpty()) && (players == null || players.isEmpty()))
            this.unloadChunk(chunkPos);
        Set<ChunkPos> loadedChunksPerPlayer = this.loadedChunksPerPlayer.get(player);
        if(loadedChunksPerPlayer != null){
            loadedChunksPerPlayer.remove(chunkPos);
            if(loadedChunksPerPlayer.isEmpty())
                this.loadedChunksPerPlayer.remove(player);
        }

        this.sendToAllPlayers(new PackedStopLoadingChunk(player, chunkPos));
    }

    /**
     * Called when a player becomes active/inactive
     */
    public void togglePlayerActivity(UUID player, boolean active){
        Set<ChunkPos> chunks = this.loadedChunksPerPlayer.get(player);
        if(chunks != null){
            if(active){
                for(ChunkPos chunk : chunks){
                    Set<UUID> inactivePlayers = this.inactivePlayersPerLoadedChunk.get(chunk);
                    if(inactivePlayers != null){
                        inactivePlayers.remove(player);
                        if(inactivePlayers.isEmpty())
                            this.inactivePlayersPerLoadedChunk.remove(chunk);
                    }

                    Set<UUID> activePlayers = this.activePlayersPerLoadedChunk.computeIfAbsent(chunk, c -> new HashSet<>());
                    if(activePlayers.isEmpty())
                        this.loadChunk(chunk);
                    activePlayers.add(player);
                }
            }else{
                for(ChunkPos chunk : chunks){
                    Set<UUID> activePlayers = this.activePlayersPerLoadedChunk.get(chunk);
                    if(activePlayers != null){
                        activePlayers.remove(player);
                        if(activePlayers.isEmpty()){
                            this.activePlayersPerLoadedChunk.remove(chunk);
                            this.unloadChunk(chunk);
                        }
                    }
                    this.inactivePlayersPerLoadedChunk.putIfAbsent(chunk, new HashSet<>());
                    this.inactivePlayersPerLoadedChunk.get(chunk).add(player);
                }
            }

            this.sendToAllPlayers(new PackedTogglePlayerActivity(player, active));
        }
    }

    public Set<ChunkPos> getChunksToBeTicked(){
        return this.activePlayersPerLoadedChunk.keySet();
    }

    private void loadChunk(ChunkPos pos){
        ForgeChunkManager.forceChunk(this.ticket, pos);
    }

    private void unloadChunk(ChunkPos pos){
        ForgeChunkManager.unforceChunk(this.ticket, pos);
    }

    public void onLoadLevel(List<ForgeChunkManager.Ticket> tickets){
        // Clear all tickets and reload the ones from the capability to naturally fix possible errors when the world is reloaded
        tickets.forEach(ForgeChunkManager::releaseTicket);
        this.ticket = ForgeChunkManager.requestTicket(ChunkLoaders.instance, this.level, ForgeChunkManager.Type.NORMAL);
        for(ChunkPos pos : this.activePlayersPerLoadedChunk.keySet())
            this.loadChunk(pos);
    }

    private void sendToAllPlayers(BasePacket packet){
        ChunkLoaders.CHANNEL.sendToDimension(this.level, packet);
    }

    public NBTTagCompound writeClientInfo(){
        NBTTagCompound compound = new NBTTagCompound();

        // Write chunkLoaderCacheMap
        NBTTagList chunkLoaderCachesTag = new NBTTagList();
        for(ChunkLoaderCache cache : this.chunkLoaderCacheMap.values())
            chunkLoaderCachesTag.appendTag(cache.write());
        compound.setTag("chunkLoaderCaches", chunkLoaderCachesTag);

        // Write loadedChunksPerPlayer
        NBTTagList loadedChunksPerActivePlayerTag = new NBTTagList();
        NBTTagList loadedChunksPerInactivePlayerTag = new NBTTagList();
        for(Map.Entry<UUID,Set<ChunkPos>> entry : this.loadedChunksPerPlayer.entrySet()){
            NBTTagCompound playerTag = new NBTTagCompound();
            playerTag.setUniqueId("player", entry.getKey());
            NBTTagList chunksTag = new NBTTagList();
            entry.getValue().stream().map(pos -> {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("chunkX", pos.x);
                tag.setInteger("chunkZ", pos.z);
                return tag;
            }).forEach(chunksTag::appendTag);
            playerTag.setTag("chunks", chunksTag);
            if(PlayerActivityTracker.isPlayerActive(entry.getKey()))
                loadedChunksPerActivePlayerTag.appendTag(playerTag);
            else
                loadedChunksPerInactivePlayerTag.appendTag(playerTag);
        }
        compound.setTag("loadedChunksPerActivePlayer", loadedChunksPerActivePlayerTag);
        compound.setTag("loadedChunksPerInactivePlayer", loadedChunksPerInactivePlayerTag);

        return compound;
    }
}
