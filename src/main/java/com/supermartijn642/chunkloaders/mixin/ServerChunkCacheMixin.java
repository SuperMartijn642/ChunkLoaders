package com.supermartijn642.chunkloaders.mixin;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

/**
 * Created 21/02/2023 by SuperMartijn642
 */
@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {

    @Shadow
    private ServerLevel level;

    @Inject(
        method = "tickChunks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z",
            shift = At.Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void tickChunks(CallbackInfo ci, long l, long m, ProfilerFiller profilerFiller, List<?> chunks, int j, NaturalSpawner.SpawnState spawnState, boolean bl3, int randomTickSpeed, boolean bl2, Iterator<?> chunkIterator, ServerChunkCache.ChunkAndHolder chunkAndHolder, LevelChunk chunk, ChunkPos chunkPos){
        //noinspection DataFlowIssue
        ServerChunkCache cache = (ServerChunkCache)(Object)this;
        if((!this.level.isNaturalSpawningAllowed(chunkPos) || !cache.chunkMap.anyPlayerCloseEnoughForSpawning(chunkPos) || !this.level.shouldTickBlocksAt(chunkPos.toLong()))
            && ChunkLoadingCapability.get(this.level).castServer().getChunksToBeTicked().contains(chunkPos))
            this.level.tickChunk(chunk, randomTickSpeed);
    }
}
