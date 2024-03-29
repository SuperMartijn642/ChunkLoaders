package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.model.data.EmptyModelData;

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
    public void render(ChunkLoaderBlockEntity entity, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int combinedLight, int combinedOverlay){
        poseStack.pushPose();

        double offset = Math.sin((System.currentTimeMillis() + entity.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        poseStack.translate(0, offset, 0);

        poseStack.translate(0.5, 0.5, 0.5);
        if(this.fullRotation){
            float angleX = (System.currentTimeMillis() + entity.animationOffset) % 13000 / 13000f * 360f;
            float angleY = (System.currentTimeMillis() + entity.animationOffset) % 15000 / 15000f * 360f;
            float angleZ = (System.currentTimeMillis() + entity.animationOffset) % 16000 / 16000f * 360f;
            poseStack.mulPose(new Quaternion(angleX, angleY, angleZ, true));
        }else{
            float angle = (System.currentTimeMillis() + entity.animationOffset) % 11000 / 11000f * 360f;
            poseStack.mulPose(new Quaternion(0, angle, 0, true));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        for(RenderType type : RenderType.chunkBufferLayers()){
            if(RenderTypeLookup.canRenderInLayer(this.block.defaultBlockState(), type)){
                IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(this.block.defaultBlockState());
                ClientUtils.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(type), this.block.defaultBlockState(), model, 1, 1, 1, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
            }
        }

        poseStack.popPose();
    }
}
