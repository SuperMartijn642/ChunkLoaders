package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderTileRenderer extends TileEntityRenderer<ChunkLoaderTile> {

    private final Block block;
    private final boolean fullRotation;

    public ChunkLoaderTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn, Block block, boolean fullRotation){
        super(rendererDispatcherIn);
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public void render(ChunkLoaderTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay){
        matrixStack.pushPose();

        double offset = Math.sin((System.currentTimeMillis() + tile.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        matrixStack.translate(0, offset, 0);

        matrixStack.translate(0.5, 0.5, 0.5);
        if(fullRotation){
            float angleX = (System.currentTimeMillis() + tile.animationOffset) % 13000 / 13000f * 360f;
            float angleY = (System.currentTimeMillis() + tile.animationOffset) % 15000 / 15000f * 360f;
            float angleZ = (System.currentTimeMillis() + tile.animationOffset) % 16000 / 16000f * 360f;
            matrixStack.mulPose(new Quaternion(angleX, angleY, angleZ, true));
        }else{
            float angle = (System.currentTimeMillis() + tile.animationOffset) % 11000 / 11000f * 360f;
            matrixStack.mulPose(new Quaternion(0, angle, 0, true));
        }
        matrixStack.translate(-0.5, -0.5, -0.5);

        for(RenderType type : RenderType.chunkBufferLayers()){
            if(RenderTypeLookup.canRenderInLayer(block.defaultBlockState(), type)){
                BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
                IBakedModel model = blockRenderer.getBlockModel(block.defaultBlockState());
                blockRenderer.getModelRenderer().renderModel(tile.getLevel(), model, block.defaultBlockState(), tile.getBlockPos(), matrixStack, buffer.getBuffer(type), false, new Random(), 0, combinedOverlay, EmptyModelData.INSTANCE);
            }
        }

        matrixStack.popPose();
    }
}
