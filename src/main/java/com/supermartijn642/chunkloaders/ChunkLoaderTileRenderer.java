package com.supermartijn642.chunkloaders;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.function.Supplier;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderTileRenderer extends TileEntitySpecialRenderer<ChunkLoaderTile> {

    private final Supplier<Block> block;
    private final boolean fullRotation;

    public ChunkLoaderTileRenderer(Supplier<Block> block, boolean fullRotation){
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public void render(ChunkLoaderTile tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        double offset = Math.sin((System.currentTimeMillis() + tile.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        GlStateManager.translate(0, offset, 0);

        GlStateManager.translate(0.5, 0.5, 0.5);
        if(fullRotation){
            float angleX = (System.currentTimeMillis() + tile.animationOffset) % 13000 / 13000f * 360f;
            float angleY = (System.currentTimeMillis() + tile.animationOffset) % 15000 / 15000f * 360f;
            float angleZ = (System.currentTimeMillis() + tile.animationOffset) % 16000 / 16000f * 360f;
            GlStateManager.rotate(angleX, 1, 0, 0);
            GlStateManager.rotate(angleY, 0, 1, 0);
            GlStateManager.rotate(angleZ, 0, 0, 1);
        }else{
            float angle = (System.currentTimeMillis() + tile.animationOffset) % 11000 / 11000f * 360f;
            GlStateManager.rotate(angle, 0, 1, 0);
        }
        GlStateManager.translate(-0.5, -0.5, -0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        GlStateManager.disableLighting();

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        try{
            BlockRendererDispatcher brd = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBakedModel model = brd.getModelForState(this.block.get().getDefaultState());
            brd.getBlockModelRenderer().renderModel(tile.getWorld(), model, this.block.get().getDefaultState(), tile.getPos(), buffer, false);
        }catch(Exception e){
            e.printStackTrace();
        }

        GlStateManager.translate(-tile.getPos().getX(), -tile.getPos().getY(), -tile.getPos().getZ());

        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
