package com.supermartijn642.chunkloaders.screen;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;

/**
 * Created 8/19/2020 by SuperMartijn642
 */
public class ChunkImage {

    private final World world;
    private final ChunkPos chunkPos;
    private final int yLevel;
    private DynamicTexture texture;

    public ChunkImage(World world, ChunkPos chunkPos, int yLevel){
        this.world = world;
        this.chunkPos = chunkPos;
        this.yLevel = yLevel;
    }

    public void bindTexture(){
        if(this.texture == null)
            this.texture = new DynamicTexture(this.createImage());
        GlStateManager.bindTexture(this.texture.getGlTextureId());
    }

    public void dispose(){
        if(this.texture != null){
            this.texture.deleteGlTexture();
            this.texture = null;
        }
    }

    private BufferedImage createImage(){
        int width = 16;
        int height = 16;

        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        for(int x = 0; x < width; x++){
            for(int z = 0; z < height; z++){
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
                MapColor color = state.getMapColor(this.world, pos);
                // Apparently blocks can return null map color #66
                int rgb = color == null ? MapColor.AIR.colorValue : color.colorValue;

                int red = ((rgb >> 16) & 255);
                int green = ((rgb >> 8) & 255);
                int blue = (rgb & 255);

                if((pos.getY() > northY && northY >= 0) || (pos.getY() > westY && westY >= 0)){
                    if(red == 0 && green == 0 && blue == 0){
                        red = 3;
                        green = 3;
                        blue = 3;
                    }else{
                        if(red > 0 && red < 3) red = 3;
                        if(green > 0 && green < 3) green = 3;
                        if(blue > 0 && blue < 3) blue = 3;
                        red = Math.min((int)(red / 0.7), 255);
                        green = Math.min((int)(green / 0.7), 255);
                        blue = Math.min((int)(blue / 0.7), 255);
                    }
                }
                if((pos.getY() < northY && northY >= 0) || (pos.getY() < westY && westY >= 0)){
                    red = Math.max((int)(red * 0.7), 0);
                    green = Math.max((int)(green * 0.7), 0);
                    blue = Math.max((int)(blue * 0.7), 0);
                }

                image.setRGB(x, z, (255 << 24) | (red << 16) | (green << 8) | blue);
            }
        }

        return image;
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
