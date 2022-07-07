package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderBlockEntityRenderer extends TileEntityRenderer<ChunkLoaderBlockEntity> {

    private final Block block;
    private final boolean fullRotation;

    public ChunkLoaderBlockEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn, Block block, boolean fullRotation){
        super(rendererDispatcherIn);
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public void render(ChunkLoaderBlockEntity tile, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        poseStack.pushPose();

        double offset = Math.sin((System.currentTimeMillis() + tile.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        poseStack.translate(0, offset, 0);

        poseStack.translate(0.5, 0.5, 0.5);
        if(this.fullRotation){
            float angleX = (System.currentTimeMillis() + tile.animationOffset) % 13000 / 13000f * 360f;
            float angleY = (System.currentTimeMillis() + tile.animationOffset) % 15000 / 15000f * 360f;
            float angleZ = (System.currentTimeMillis() + tile.animationOffset) % 16000 / 16000f * 360f;
            poseStack.mulPose(new Quaternion(angleX, angleY, angleZ, true));
        }else{
            float angle = (System.currentTimeMillis() + tile.animationOffset) % 11000 / 11000f * 360f;
            poseStack.mulPose(new Quaternion(0, angle, 0, true));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        for(RenderType type : RenderType.chunkBufferLayers()){
            if(RenderTypeLookup.canRenderInLayer(this.block.defaultBlockState(), type)){
                BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
                IBakedModel model = blockRenderer.getBlockModel(this.block.defaultBlockState());
                blockRenderer.getModelRenderer().renderModel(tile.getLevel(), model, this.block.defaultBlockState(), tile.getBlockPos(), poseStack, buffer.getBuffer(type), false, new Random(), 0, combinedOverlay, EmptyModelData.INSTANCE);
            }
        }

        poseStack.popPose();
    }
}
