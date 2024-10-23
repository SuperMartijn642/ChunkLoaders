package com.supermartijn642.chunkloaders.mixin;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created 21/02/2023 by SuperMartijn642
 */
@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Final
    @Shadow
    private ServerLevel level;
    @Final
    @Shadow
    private List<LevelChunk> tickingChunks;
    @Unique
    private final Set<ChunkPos> tickingChunksSet = new HashSet<>();

    @Inject(
        method = "tickChunks()V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;collectTickingChunks(Ljava/util/List;)V",
            shift = At.Shift.AFTER
        )
    )
    private void addChunkLoadedChunksForTicking(CallbackInfo ci){
        // Put all chunks currently in the list into a set, so we can look them up quickly
        for(LevelChunk levelChunk : this.tickingChunks)
            this.tickingChunksSet.add(levelChunk.getPos());
        // Go through all chunk loaded chunks
        for(ChunkPos pos : ChunkLoadingCapability.get(this.level).castServer().getChunksToBeTicked()){
            // Check the chunk is not already in the list
            if(this.tickingChunksSet.contains(pos))
                continue;
            // Get the chunk and add it to the list
            LevelChunk chunk = this.level.getChunk(pos.x, pos.z);
            this.tickingChunks.add(chunk);
        }
        this.tickingChunksSet.clear();
    }
}
