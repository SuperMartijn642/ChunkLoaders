package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
public class ClientChunkLoadingCapability extends ChunkLoadingCapability {

    public ClientChunkLoadingCapability(World level){
        super(level);
    }

    public void addChunkLoader(BlockPos pos, UUID owner, ChunkLoaderType type){
        ChunkPos chunkPos = new ChunkPos(pos);
        this.chunkLoadersPerChunk.putIfAbsent(chunkPos, new HashSet<>());
        this.chunkLoadersPerChunk.get(chunkPos).add(pos);
        this.chunkLoadersPerPlayer.putIfAbsent(owner, new HashSet<>());
        this.chunkLoadersPerPlayer.get(owner).add(pos);
        this.chunkLoaderCacheMap.put(pos, new ChunkLoaderCache(pos, type, owner));

        int centerChunkX = pos.getX() >> 4, centerChunkZ = pos.getZ() >> 4;
        int range = type.getRange();
        this.availableChunksPerPlayer.putIfAbsent(owner, new HashSet<>());
        for(int x = -range + 1; x < range; x++){
            for(int z = -range + 1; z < range; z++){
                this.availableChunksPerPlayer.get(owner).add(new ChunkPos(centerChunkX + x, centerChunkZ + z));
            }
        }
    }

    public void removeChunkLoader(BlockPos pos, UUID owner, ChunkLoaderType type){
        ChunkPos chunkPos = new ChunkPos(pos);
        Set<BlockPos> chunkLoadersPerChunk = this.chunkLoadersPerChunk.get(chunkPos);
        if(chunkLoadersPerChunk != null && chunkLoadersPerChunk.contains(pos)){
            chunkLoadersPerChunk.remove(pos);
            if(chunkLoadersPerChunk.isEmpty())
                this.chunkLoadersPerChunk.remove(chunkPos);

            Set<BlockPos> chunkLoadersPerPlayer = this.chunkLoadersPerPlayer.get(owner);
            chunkLoadersPerPlayer.remove(pos);
            if(chunkLoadersPerPlayer.isEmpty())
                this.chunkLoadersPerPlayer.remove(owner);

            this.chunkLoaderCacheMap.remove(pos);

            Set<ChunkPos> loadedChunksPerPlayer = this.loadedChunksPerPlayer.get(owner);
            Set<ChunkPos> availableChunksPerPlayer = this.availableChunksPerPlayer.get(owner);
            int radius = type.getRange();
            for(int x = -radius + 1; x < radius; x++){
                loop:
                for(int z = -radius + 1; z < radius; z++){
                    ChunkPos otherChunk = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                    if(this.chunkLoadersPerPlayer.containsKey(owner)){
                        for(BlockPos chunkLoaderPos : this.chunkLoadersPerPlayer.get(owner)){
                            int chunkLoaderChunkX = chunkLoaderPos.getX() >> 4, chunkLoaderChunkZ = chunkLoaderPos.getZ() >> 4;
                            ChunkLoaderType otherType = this.chunkLoaderCacheMap.get(chunkLoaderPos).chunkLoaderType;
                            if(Math.abs(chunkLoaderChunkX - otherChunk.x) < otherType.getRange() && Math.abs(chunkLoaderChunkZ - otherChunk.z) < otherType.getRange())
                                continue loop;
                        }
                    }

                    Set<UUID> inactivePlayer = this.inactivePlayersPerLoadedChunk.get(chunkPos);
                    if(inactivePlayer != null){
                        inactivePlayer.remove(owner);
                        if(inactivePlayer.isEmpty())
                            this.inactivePlayersPerLoadedChunk.remove(chunkPos);
                    }
                    Set<UUID> players = this.activePlayersPerLoadedChunk.get(chunkPos);
                    if(players != null){
                        players.remove(owner);
                        if(players.isEmpty())
                            this.activePlayersPerLoadedChunk.remove(chunkPos);
                    }

                    if(loadedChunksPerPlayer != null)
                        loadedChunksPerPlayer.remove(otherChunk);
                    if(availableChunksPerPlayer != null)
                        availableChunksPerPlayer.remove(otherChunk);
                }
            }
            if(loadedChunksPerPlayer != null && loadedChunksPerPlayer.isEmpty())
                this.loadedChunksPerPlayer.remove(owner);
            if(availableChunksPerPlayer != null && availableChunksPerPlayer.isEmpty())
                this.availableChunksPerPlayer.remove(owner);
        }
    }

    public void startLoadingChunk(UUID player, ChunkPos chunkPos, boolean active){
        if(active){
            this.activePlayersPerLoadedChunk.putIfAbsent(chunkPos, new HashSet<>());
            this.activePlayersPerLoadedChunk.get(chunkPos).add(player);
        }else{
            this.inactivePlayersPerLoadedChunk.putIfAbsent(chunkPos, new HashSet<>());
            this.inactivePlayersPerLoadedChunk.get(chunkPos).add(player);
        }
        this.loadedChunksPerPlayer.putIfAbsent(player, new HashSet<>());
        this.loadedChunksPerPlayer.get(player).add(chunkPos);
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

        Set<ChunkPos> loadedChunksPerPlayer = this.loadedChunksPerPlayer.get(player);
        if(loadedChunksPerPlayer != null){
            loadedChunksPerPlayer.remove(chunkPos);
            if(loadedChunksPerPlayer.isEmpty())
                this.loadedChunksPerPlayer.remove(player);
        }
    }

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
                    this.activePlayersPerLoadedChunk.putIfAbsent(chunk, new HashSet<>());
                    this.activePlayersPerLoadedChunk.get(chunk).add(player);
                }
            }else{
                for(ChunkPos chunk : chunks){
                    Set<UUID> activePlayers = this.activePlayersPerLoadedChunk.get(chunk);
                    if(activePlayers != null){
                        activePlayers.remove(player);
                        if(activePlayers.isEmpty())
                            this.activePlayersPerLoadedChunk.remove(chunk);
                    }
                    this.inactivePlayersPerLoadedChunk.putIfAbsent(chunk, new HashSet<>());
                    this.inactivePlayersPerLoadedChunk.get(chunk).add(player);
                }
            }
        }
    }

    public void readServerInfo(CompoundNBT compound){
        // Read chunkLoaderCacheMap
        ListNBT chunkLoaderCachesTag = compound.getList("chunkLoaderCaches", Constants.NBT.TAG_COMPOUND);
        chunkLoaderCachesTag.stream().map(CompoundNBT.class::cast).map(ChunkLoaderCache::read).forEach(
            cache -> {
                this.chunkLoaderCacheMap.put(cache.chunkLoaderPos, cache);
                this.chunkLoadersPerChunk.putIfAbsent(cache.chunkPos, new HashSet<>());
                this.chunkLoadersPerChunk.get(cache.chunkPos).add(cache.chunkLoaderPos);
                this.chunkLoadersPerPlayer.putIfAbsent(cache.owner, new HashSet<>());
                this.chunkLoadersPerPlayer.get(cache.owner).add(cache.chunkLoaderPos);

                int range = cache.chunkLoaderType.getRange();
                this.availableChunksPerPlayer.putIfAbsent(cache.owner, new HashSet<>());
                for(int x = -range + 1; x < range; x++){
                    for(int z = -range + 1; z < range; z++){
                        this.availableChunksPerPlayer.get(cache.owner).add(new ChunkPos(cache.chunkPos.x + x, cache.chunkPos.z + z));
                    }
                }
            }
        );

        // Read loadedChunksPerPlayer for active players
        ListNBT loadedChunksPerActivePlayerTag = compound.getList("loadedChunksPerActivePlayer", Constants.NBT.TAG_COMPOUND);
        loadedChunksPerActivePlayerTag.stream().map(CompoundNBT.class::cast).forEach(
            playerTag -> {
                UUID player = playerTag.getUUID("player");
                Collection<ChunkPos> chunks = Arrays.stream(playerTag.getLongArray("chunks")).mapToObj(ChunkPos::new).collect(Collectors.toList());
                this.loadedChunksPerPlayer.putIfAbsent(player, new HashSet<>());
                this.loadedChunksPerPlayer.get(player).addAll(chunks);
                for(ChunkPos chunk : chunks){
                    this.activePlayersPerLoadedChunk.putIfAbsent(chunk, new HashSet<>());
                    this.activePlayersPerLoadedChunk.get(chunk).add(player);
                }
            }
        );

        // Read loadedChunksPerPlayer for inactive players
        ListNBT loadedChunksPerInactivePlayerTag = compound.getList("loadedChunksPerInactivePlayer", Constants.NBT.TAG_COMPOUND);
        loadedChunksPerInactivePlayerTag.stream().map(CompoundNBT.class::cast).forEach(
            playerTag -> {
                UUID player = playerTag.getUUID("player");
                Collection<ChunkPos> chunks = Arrays.stream(playerTag.getLongArray("chunks")).mapToObj(ChunkPos::new).collect(Collectors.toList());
                this.loadedChunksPerPlayer.putIfAbsent(player, new HashSet<>());
                this.loadedChunksPerPlayer.get(player).addAll(chunks);
                for(ChunkPos chunk : chunks){
                    this.inactivePlayersPerLoadedChunk.putIfAbsent(chunk, new HashSet<>());
                    this.inactivePlayersPerLoadedChunk.get(chunk).add(player);
                }
            }
        );
    }
}
