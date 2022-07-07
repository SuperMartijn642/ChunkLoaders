package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.ChunkLoadersConfig;
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
public class ChunkLoadingCapability {

    public static ChunkLoadingCapability get(World level){
        return level.getCapability(ChunkLoaders.CHUNK_LOADING_CAPABILITY).orElse(null);
    }

    protected final World level;

    /**
     * Chunks actually loaded by players
     */
    protected final Map<ChunkPos,Set<UUID>> activePlayersPerLoadedChunk = new HashMap<>();
    /**
     * Chunks actually loaded by players who've been inactive too long
     */
    protected final Map<ChunkPos,Set<UUID>> inactivePlayersPerLoadedChunk = new HashMap<>();
    /**
     * Maps chunks to the chunk loaders placed in them
     */
    protected final Map<ChunkPos,Set<BlockPos>> chunkLoadersPerChunk = new HashMap<>();
    /**
     * Maps players to the chunks they're loading
     */
    protected final Map<UUID,Set<ChunkPos>> loadedChunksPerPlayer = new HashMap<>();
    /**
     * Maps players to the chunks which are in range their chunk loaders
     */
    protected final Map<UUID,Set<ChunkPos>> availableChunksPerPlayer = new HashMap<>();
    /**
     * Maps players to the chunk loaders they own
     */
    protected final Map<UUID,Set<BlockPos>> chunkLoadersPerPlayer = new HashMap<>();
    /**
     * Maps chunk loader position to a cache of their settings
     */
    protected final Map<BlockPos,ChunkLoaderCache> chunkLoaderCacheMap = new HashMap<>();

    public ChunkLoadingCapability(World level){
        this.level = level;
    }

    public ServerChunkLoadingCapability castServer(){
        return (ServerChunkLoadingCapability)this;
    }

    public ClientChunkLoadingCapability castClient(){
        return (ClientChunkLoadingCapability)this;
    }

    public boolean isChunkLoadedByPlayer(UUID player, ChunkPos chunkPos){
        return this.loadedChunksPerPlayer.containsKey(player) && this.loadedChunksPerPlayer.get(player).contains(chunkPos);
    }

    public boolean isChunkLoaded(ChunkPos chunkPos){
        return this.activePlayersPerLoadedChunk.containsKey(chunkPos) || this.inactivePlayersPerLoadedChunk.containsKey(chunkPos);
    }

    public Set<ChunkPos> getChunksLoadedByPlayer(UUID player){
        return Collections.unmodifiableSet(this.loadedChunksPerPlayer.getOrDefault(player, Collections.emptySet()));
    }

    public Set<UUID> getActivePlayersLoadingChunk(ChunkPos chunkPos){
        return Collections.unmodifiableSet(this.activePlayersPerLoadedChunk.getOrDefault(chunkPos, Collections.emptySet()));
    }

    public Set<UUID> getInactivePlayersLoadingChunk(ChunkPos chunkPos){
        return Collections.unmodifiableSet(this.inactivePlayersPerLoadedChunk.getOrDefault(chunkPos, Collections.emptySet()));
    }

    public boolean canPlayerLoadChunk(UUID player, ChunkPos chunkPos){
        int maxLoadedChunks = ChunkLoadersConfig.maxLoadedChunksPerPlayer.get();
        return (maxLoadedChunks <= 0 || !this.loadedChunksPerPlayer.containsKey(player) || this.loadedChunksPerPlayer.get(player).size() < maxLoadedChunks)
            && this.availableChunksPerPlayer.containsKey(player) && this.availableChunksPerPlayer.get(player).contains(chunkPos);
    }

    public CompoundNBT write(){
        CompoundNBT compound = new CompoundNBT();

        // Write chunkLoaderCacheMap
        ListNBT chunkLoaderCachesTag = new ListNBT();
        for(ChunkLoaderCache cache : this.chunkLoaderCacheMap.values())
            chunkLoaderCachesTag.add(cache.write());
        compound.put("chunkLoaderCaches", chunkLoaderCachesTag);

        // Write loadedChunksPerPlayer
        ListNBT loadedChunksPerPlayerTag = new ListNBT();
        for(Map.Entry<UUID,Set<ChunkPos>> entry : this.loadedChunksPerPlayer.entrySet()){
            CompoundNBT playerTag = new CompoundNBT();
            playerTag.putUUID("player", entry.getKey());
            playerTag.putLongArray("chunks", entry.getValue().stream().mapToLong(ChunkPos::toLong).toArray());
            loadedChunksPerPlayerTag.add(playerTag);
        }
        compound.put("loadedChunksPerPlayer", loadedChunksPerPlayerTag);

        return compound;
    }

    public void read(CompoundNBT compound){
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

        // Read loadedChunksPerPlayer
        ListNBT loadedChunksPerPlayerTag = compound.getList("loadedChunksPerPlayer", Constants.NBT.TAG_COMPOUND);
        loadedChunksPerPlayerTag.stream().map(CompoundNBT.class::cast).forEach(
            playerTag -> {
                UUID player = playerTag.getUUID("player");
                Collection<ChunkPos> chunks = Arrays.stream(playerTag.getLongArray("chunks")).mapToObj(ChunkPos::new).collect(Collectors.toList());
                this.loadedChunksPerPlayer.putIfAbsent(player, new HashSet<>());
                this.loadedChunksPerPlayer.get(player).addAll(chunks);
                for(ChunkPos chunk : chunks){
                    if(PlayerActivityTracker.isPlayerActive(player)){
                        this.activePlayersPerLoadedChunk.putIfAbsent(chunk, new HashSet<>());
                        this.activePlayersPerLoadedChunk.get(chunk).add(player);
                    }else{
                        this.inactivePlayersPerLoadedChunk.putIfAbsent(chunk, new HashSet<>());
                        this.inactivePlayersPerLoadedChunk.get(chunk).add(player);
                    }
                }
            }
        );
    }
}
