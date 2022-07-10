package com.supermartijn642.chunkloaders.screen;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
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
    private final int yLevel;
    public int textureId = -1;

    public ChunkImage(World world, ChunkPos chunkPos, int yLevel){
        this.world = world;
        this.chunkPos = chunkPos;
        this.yLevel = yLevel;
    }

    public void updateTexture(){
        if(this.textureId == -1){
            ByteBuffer buffer = this.createBuffer();

            this.textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 16, 16, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
        }
    }

    private ByteBuffer createBuffer(){
        int width = 16;
        int height = 16;

        ByteBuffer rgbBuffer = ByteBuffer.allocateDirect(width * height * 3);

        for(int z = 0; z < height; z++){
            for(int x = 0; x < width; x++){
                BlockPos pos;
                int northY, westY;
                if(this.shouldDrawAtSameLayer()){
                    pos = this.getFirstBlockGoingDown(this.chunkPos.getXStart() + x, this.yLevel + 1, this.chunkPos.getZStart() + z, 5);
                    northY = this.getFirstBlockGoingDown(this.chunkPos.getXStart() + x, this.yLevel + 1, this.chunkPos.getZStart() + z - 1, 6).getY();
                    westY = this.getFirstBlockGoingDown(this.chunkPos.getXStart() + x - 1, this.yLevel + 1, this.chunkPos.getZStart() + z, 6).getY();
                }else{
                    pos = this.world.getHeight(new BlockPos(this.chunkPos.getXStart() + x, 0, this.chunkPos.getZStart() + z)).down();
                    northY = this.world.getHeight(pos.getX(), pos.getZ() - 1) - 1;
                    westY = this.world.getHeight(pos.getX() - 1, pos.getZ()) - 1;
                }

                IBlockState state = this.world.getBlockState(pos);
                int rgb = state.getMapColor(this.world, pos).colorValue;

                Color color = new Color(rgb);
                if((pos.getY() > northY && northY >= 0) || (pos.getY() > westY && westY >= 0))
                    color = color.brighter();
                if((pos.getY() < northY && northY >= 0) || (pos.getY() < westY && westY >= 0))
                    color = color.darker();
                rgb = color.getRGB();

                rgbBuffer.put((byte)((rgb >> 16) & 255));
                rgbBuffer.put((byte)((rgb >> 8) & 255));
                rgbBuffer.put((byte)(rgb & 255));
            }
        }

        rgbBuffer.flip();
        return rgbBuffer;
    }

    private BlockPos getFirstBlockGoingDown(int x, int y, int z, int maxTries){
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        int tries = 0;
        while(this.world.isAirBlock(pos) && ++tries < maxTries)
            pos.setY(pos.getY() - 1);

        return pos;
    }

    /**
     * If true, the image will simply take blocks from the same y-level instead of from the top of the world.
     */
    private boolean shouldDrawAtSameLayer(){
        return this.world.provider.isNether();
    }
}
