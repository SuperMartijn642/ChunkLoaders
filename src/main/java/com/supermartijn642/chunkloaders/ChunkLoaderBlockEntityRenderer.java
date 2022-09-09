package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.lwjgl.opengl.GL11;

import java.util.Random;

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
    public void render(ChunkLoaderBlockEntity entity, float partialTicks, int combinedOverlay){
        GlStateManager.pushMatrix();

        double offset = Math.sin((System.currentTimeMillis() + entity.animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        GlStateManager.translated(0, offset, 0);

        GlStateManager.translated(0.5, 0.5, 0.5);
        if(this.fullRotation){
            float angleX = (System.currentTimeMillis() + entity.animationOffset) % 13000 / 13000f * 360f;
            float angleY = (System.currentTimeMillis() + entity.animationOffset) % 15000 / 15000f * 360f;
            float angleZ = (System.currentTimeMillis() + entity.animationOffset) % 16000 / 16000f * 360f;
            GlStateManager.rotated(angleX, 1, 0, 0);
            GlStateManager.rotated(angleY, 0, 1, 0);
            GlStateManager.rotated(angleZ, 0, 0, 1);
        }else{
            float angle = (System.currentTimeMillis() + entity.animationOffset) % 11000 / 11000f * 360f;
            GlStateManager.rotated(angle, 0, 1, 0);
        }
        GlStateManager.translated(-0.5, -0.5, -0.5);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        GlStateManager.disableLighting();

        ScreenUtils.bindTexture(AtlasTexture.LOCATION_BLOCKS);

        IBakedModel model = ClientUtils.getBlockRenderer().getBlockModel(this.block.defaultBlockState());
        IModelData data = model.getModelData(entity.getLevel(), entity.getBlockPos(), this.block.defaultBlockState(), EmptyModelData.INSTANCE);
        ClientUtils.getBlockRenderer().getModelRenderer().renderModel(entity.getLevel(), model, this.block.defaultBlockState(), entity.getBlockPos(), buffer, false, new Random(), 0, data);

        GlStateManager.translated(-entity.getBlockPos().getX(), -entity.getBlockPos().getY(), -entity.getBlockPos().getZ());

        tessellator.end();

        GlStateManager.popMatrix();
    }
}
