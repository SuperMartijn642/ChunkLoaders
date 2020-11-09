package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.nio.ByteBuffer;

/**
 * Created 8/19/2020 by SuperMartijn642
 */
public class ChunkImage {

    private final World world;
    private final ChunkPos chunkPos;
    public int textureId = -1;
    private byte[] buffer = null;

    public ChunkImage(World world, ChunkPos chunkPos){
        this.world = world;
        this.chunkPos = chunkPos;
    }

    public void createTexture(){
        this.textureId = GlStateManager.genTexture();
    }

    public void updateTexture(){
        if(this.buffer == null)
            this.buffer = this.createBuffer();

        GlStateManager.bindTexture(this.textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        ByteBuffer buffer = ByteBuffer.allocateDirect(this.buffer.length);
        buffer.put(this.buffer).flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB8, 16, 16, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    private byte[] createBuffer(){
        int width = 16;
        int height = 16;

        byte[] rgbArray = new byte[width * height * 3];

        for(int x = 0; x < width; x++){
            for(int z = 0; z < height; z++){
                BlockPos pos = this.world.getHeight(Heightmap.Type.WORLD_SURFACE, new BlockPos(this.chunkPos.getXStart() + x, 0, this.chunkPos.getZStart() + z)).down();
                int northY = this.world.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ() - 1) - 1;
                int westY = this.world.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX() - 1, pos.getZ()) - 1;

                BlockState state = this.world.getBlockState(pos);
                int rgb = state.getMaterialColor(this.world, pos).colorValue;

                Color color = new Color(rgb);
                if((pos.getY() > northY && northY >= 0) || (pos.getY() > westY && westY >= 0))
                    color = color.brighter();
                if((pos.getY() < northY && northY >= 0) || (pos.getY() < westY && westY >= 0))
                    color = color.darker();
                rgb = color.getRGB();

                int index = (x * height + z) * 3;
                rgbArray[index] = (byte)((rgb >> 16) & 255);
                rgbArray[index + 1] = (byte)(double)((rgb >> 8) & 255);
                rgbArray[index + 2] = (byte)(double)(rgb & 255);
            }
        }

        return rgbArray;
    }

}
