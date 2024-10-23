package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import org.joml.Quaternionf;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderBlockEntityRenderer implements CustomBlockEntityRenderer<ChunkLoaderBlockEntity> {

    private final Block block;
    private final boolean fullRotation;

    public ChunkLoaderBlockEntityRenderer(Block block, boolean fullRotation){
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public void render(ChunkLoaderBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay){
        poseStack.pushPose();

        double offset = Math.sin((System.currentTimeMillis() + entity.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        poseStack.translate(0, offset, 0);

        poseStack.translate(0.5, 0.5, 0.5);
        if(this.fullRotation){
            float angleX = (System.currentTimeMillis() + entity.animationOffset) % 13000 / 13000f * 2 * (float)Math.PI;
            float angleY = (System.currentTimeMillis() + entity.animationOffset) % 15000 / 15000f * 2 * (float)Math.PI;
            float angleZ = (System.currentTimeMillis() + entity.animationOffset) % 16000 / 16000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleX, 1, 0, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleY, 0, 1, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleZ, 0, 0, 1));
        }else{
            float angle = (System.currentTimeMillis() + entity.animationOffset) % 11000 / 11000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angle, 0, 1, 0));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        RenderType type = ItemBlockRenderTypes.getRenderType(this.block.defaultBlockState());
        BakedModel model = ClientUtils.getBlockRenderer().getBlockModel(this.block.defaultBlockState());
        ClientUtils.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(type), this.block.defaultBlockState(), model, 1, 1, 1, combinedLight, combinedOverlay);

        poseStack.popPose();
    }
}
