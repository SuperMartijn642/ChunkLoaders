package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import com.supermartijn642.core.util.Holder;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.Block;
import org.joml.Quaternionf;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderBlockEntityRenderer implements CustomBlockEntityRenderer<ChunkLoaderBlockEntity,Holder<Integer>> {

    private final Block block;
    private final boolean fullRotation;

    public ChunkLoaderBlockEntityRenderer(Block block, boolean fullRotation){
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public Holder<Integer> createStateHolder(){
        return new Holder<>(0);
    }

    @Override
    public void updateState(Holder<Integer> state, ChunkLoaderBlockEntity entity, UpdateContext context){
        state.set(entity.animationOffset);
    }

    @Override
    public void submit(SubmitNodeCollector output, Holder<Integer> state, RenderContext context){
        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();

        int animationOffset = state.get();
        double offset = Math.sin((System.currentTimeMillis() + animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        poseStack.translate(0, offset, 0);

        poseStack.translate(0.5, 0.5, 0.5);
        if(this.fullRotation){
            float angleX = (System.currentTimeMillis() + animationOffset) % 13000 / 13000f * 2 * (float)Math.PI;
            float angleY = (System.currentTimeMillis() + animationOffset) % 15000 / 15000f * 2 * (float)Math.PI;
            float angleZ = (System.currentTimeMillis() + animationOffset) % 16000 / 16000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleX, 1, 0, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleY, 0, 1, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleZ, 0, 0, 1));
        }else{
            float angle = (System.currentTimeMillis() + animationOffset) % 11000 / 11000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angle, 0, 1, 0));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        RenderType renderType = ItemBlockRenderTypes.getRenderType(this.block.defaultBlockState());
        BlockStateModel model = ClientUtils.getBlockRenderer().getBlockModel(this.block.defaultBlockState());
        ModelFeatureRenderer.CrumblingOverlay breakingOverlay = context.breakingOverlay();
        output.submitBlockModel(poseStack, renderType, model, 1, 1, 1, context.packedLight(), breakingOverlay == null ? OverlayTexture.NO_OVERLAY : breakingOverlay.progress(), 0);

        poseStack.popPose();
    }
}
