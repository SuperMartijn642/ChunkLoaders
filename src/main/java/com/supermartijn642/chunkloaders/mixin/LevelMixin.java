package com.supermartijn642.chunkloaders.mixin;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ClientChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ServerChunkLoadingCapability;
import com.supermartijn642.chunkloaders.extensions.ChunkLoadersLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created 20/02/2023 by SuperMartijn642
 */
@Mixin(Level.class)
public class LevelMixin implements ChunkLoadersLevel {

    private ChunkLoadingCapability chunkloadersChunkLoadingCapability;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void constructor(CallbackInfo ci){
        //noinspection DataFlowIssue
        Level level = (Level)(Object)this;
        this.chunkloadersChunkLoadingCapability = level instanceof ServerLevel ?
            new ServerChunkLoadingCapability(level) : new ClientChunkLoadingCapability(level);
    }

    @Override
    public ChunkLoadingCapability getChunkLoadingCapability(){
        return this.chunkloadersChunkLoadingCapability;
    }
}
