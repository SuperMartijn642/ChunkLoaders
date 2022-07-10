package com.supermartijn642.chunkloaders;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.block.Block;
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
public class ChunkLoaderBlockEntityRenderer extends TileEntitySpecialRenderer<ChunkLoaderBlockEntity> {

    private final Supplier<Block> block;
    private final boolean fullRotation;

    public ChunkLoaderBlockEntityRenderer(Supplier<Block> block, boolean fullRotation){
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public void render(ChunkLoaderBlockEntity entity, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        double offset = Math.sin((System.currentTimeMillis() + entity.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        GlStateManager.translate(0, offset, 0);

        GlStateManager.translate(0.5, 0.5, 0.5);
        if(this.fullRotation){
            float angleX = (System.currentTimeMillis() + entity.animationOffset) % 13000 / 13000f * 360f;
            float angleY = (System.currentTimeMillis() + entity.animationOffset) % 15000 / 15000f * 360f;
            float angleZ = (System.currentTimeMillis() + entity.animationOffset) % 16000 / 16000f * 360f;
            GlStateManager.rotate(angleX, 1, 0, 0);
            GlStateManager.rotate(angleY, 0, 1, 0);
            GlStateManager.rotate(angleZ, 0, 0, 1);
        }else{
            float angle = (System.currentTimeMillis() + entity.animationOffset) % 11000 / 11000f * 360f;
            GlStateManager.rotate(angle, 0, 1, 0);
        }
        GlStateManager.translate(-0.5, -0.5, -0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        GlStateManager.disableLighting();

        ScreenUtils.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        IBakedModel model = ClientUtils.getBlockRenderer().getModelForState(this.block.get().getDefaultState());
        ClientUtils.getBlockRenderer().getBlockModelRenderer().renderModel(entity.getWorld(), model, this.block.get().getDefaultState(), entity.getPos(), buffer, false);

        GlStateManager.translate(-entity.getPos().getX(), -entity.getPos().getY(), -entity.getPos().getZ());

        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
