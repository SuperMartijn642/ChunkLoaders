package com.supermartijn642.chunkloaders.mixin;

import com.supermartijn642.chunkloaders.extensions.ChunkLoadersKeyMappingCategory;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created 22/12/2025 by SuperMartijn642
 */
@Mixin(KeyMapping.Category.class)
public class KeyMappingCategoryMixin implements ChunkLoadersKeyMappingCategory {

    @Unique
    private Component label;

    @Override
    public void chunkloadersOverwriteLabel(Component label){
        this.label = label;
    }

    @Inject(
        method = "label",
        at = @At("HEAD"),
        cancellable = true
    )
    private void label(CallbackInfoReturnable<Component> ci){
        if(this.label != null)
            ci.setReturnValue(this.label);
    }
}
