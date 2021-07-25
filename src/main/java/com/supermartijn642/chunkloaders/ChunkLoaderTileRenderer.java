package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.data.EmptyModelData;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderTileRenderer implements BlockEntityRenderer<ChunkLoaderTile> {

    private final Block block;
    private final boolean fullRotation;

    public ChunkLoaderTileRenderer(Block block, boolean fullRotation){
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public void render(ChunkLoaderTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay){
        matrixStack.pushPose();

        double offset = Math.sin((System.currentTimeMillis() + tile.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        matrixStack.translate(0, offset, 0);

        matrixStack.translate(0.5, 0.5, 0.5);
        if(this.fullRotation){
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
            if(ItemBlockRenderTypes.canRenderInLayer(this.block.defaultBlockState(), type)){
                BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
                BakedModel model = blockRenderer.getBlockModel(this.block.defaultBlockState());
                blockRenderer.getModelRenderer().renderModel(matrixStack.last(), buffer.getBuffer(type), block.defaultBlockState(), model, 1, 1, 1, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
            }
        }

        matrixStack.popPose();
    }
}
