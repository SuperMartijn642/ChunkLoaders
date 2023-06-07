package com.supermartijn642.chunkloaders.mixin;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapabilitySaveData;
import com.supermartijn642.chunkloaders.capability.PlayerActivityTracker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created 21/02/2023 by SuperMartijn642
 */
@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void constructor(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List<CustomSpawner> list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci){
        //noinspection DataFlowIssue
        ServerLevel level = (ServerLevel)(Object)this;
        ChunkLoadingCapabilitySaveData.init(level, ChunkLoadingCapability.get(level));
    }

    @Inject(
        method = "save",
        at = @At("TAIL")
    )
    private void save(ProgressListener progressListener, boolean bl, boolean bl2, CallbackInfo ci){
        // Save the player activity data
        if(!bl2)
            //noinspection DataFlowIssue
            PlayerActivityTracker.onWorldSave((ServerLevel)(Object)this);
    }

    @ModifyVariable(
        method = "tick",
        at = @At("STORE")
    )
    private boolean tick(boolean shouldTick){
        //noinspection DataFlowIssue
        return shouldTick || !ChunkLoadingCapability.get((ServerLevel)(Object)this).castServer().getChunksToBeTicked().isEmpty();
    }
}
