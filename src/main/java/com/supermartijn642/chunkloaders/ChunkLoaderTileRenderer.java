package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderTileRenderer extends TileEntityRenderer<ChunkLoaderTile> {

    private final Block block;
    private final boolean fullRotation;

    public ChunkLoaderTileRenderer(Block block, boolean fullRotation){
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public void render(ChunkLoaderTile tile, double x, double y, double z, float partialTicks, int destroyStage){
        GlStateManager.pushMatrix();
        GlStateManager.translated(x, y, z);

        double offset = Math.sin((System.currentTimeMillis() + tile.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        GlStateManager.translated(0, offset, 0);

        GlStateManager.translated(0.5, 0.5, 0.5);
        if(fullRotation){
            float angleX = (System.currentTimeMillis() + tile.animationOffset) % 13000 / 13000f * 360f;
            float angleY = (System.currentTimeMillis() + tile.animationOffset) % 15000 / 15000f * 360f;
            float angleZ = (System.currentTimeMillis() + tile.animationOffset) % 16000 / 16000f * 360f;
            GlStateManager.rotated(angleX, 1, 0, 0);
            GlStateManager.rotated(angleY, 0, 1, 0);
            GlStateManager.rotated(angleZ, 0, 0, 1);
        }else{
            float angle = (System.currentTimeMillis() + tile.animationOffset) % 11000 / 11000f * 360f;
            GlStateManager.rotated(angle, 0, 1, 0);
        }
        GlStateManager.translated(-0.5, -0.5, -0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        GlStateManager.disableLighting();

        Minecraft.getInstance().getTextureManager().bind(AtlasTexture.LOCATION_BLOCKS);

        IModelData data = Minecraft.getInstance().getBlockRenderer().getBlockModel(this.block.defaultBlockState()).getModelData(tile.getLevel(), tile.getBlockPos(), this.block.defaultBlockState(), EmptyModelData.INSTANCE);
        try{
            BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRenderer();
            IBakedModel model = brd.getBlockModel(this.block.defaultBlockState());
            brd.getModelRenderer().renderModel(tile.getLevel(), model, this.block.defaultBlockState(), tile.getBlockPos(), buffer, false, new Random(), 0, data);
        }catch(Exception e){
            e.printStackTrace();
        }

        GlStateManager.translated(-tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ());

        tessellator.end();

        GlStateManager.popMatrix();
    }
}
