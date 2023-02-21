package com.supermartijn642.chunkloaders.mixin;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Created 20/02/2023 by SuperMartijn642
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    @Shadow
    private Map<ResourceKey<Level>,ServerLevel> levels;

    @Inject(
        method = "prepareLevels",
        at = @At("TAIL")
    )
    private void prepareLevels(ChunkProgressListener chunkProgressListener, CallbackInfo ci){
        // Make sure all chunk loaded chunks actually get loaded again
        for(ServerLevel level : this.levels.values())
            ChunkLoadingCapability.get(level).castServer().onLoadLevel();
    }
}
