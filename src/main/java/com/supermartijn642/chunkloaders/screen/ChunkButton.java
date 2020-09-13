package com.supermartijn642.chunkloaders.screen;

import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketToggleChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.awt.*;
import java.util.function.Supplier;

/**
 * Created 9/2/2020 by SuperMartijn642
 */
public class ChunkButton extends GuiButton {

    private static final ResourceLocation BUTTON_OFF = new ResourceLocation("chunkloaders", "textures/gui/button_off.png"),
        BUTTON_ON = new ResourceLocation("chunkloaders", "textures/gui/button_on.png");

    public final int xOffset, zOffset;
    private final Supplier<ChunkLoaderTile> tileSupplier;
    private final ChunkImage image;

    public ChunkButton(int buttonId, int x, int y, int xOffset, int zOffset, Supplier<ChunkLoaderTile> tileSupplier, World world, ChunkPos chunk){
        super(buttonId, x, y, 15, 15, "");
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.tileSupplier = tileSupplier;
        this.image = new ChunkImage(world, chunk);
        this.image.createTexture();
    }

    public void onPress(){
        ChunkLoaderTile tile = this.tileSupplier.get();
        if(tile != null)
            ChunkLoaders.channel.sendToServer(new PacketToggleChunk(tile.getPos(), this.xOffset, this.zOffset));
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks){
        ChunkLoaderTile tile = this.tileSupplier.get();
        if(tile == null)
            return;
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        Minecraft.getMinecraft().getTextureManager().bindTexture(tile.isLoaded(this.xOffset, this.zOffset) ? BUTTON_ON : BUTTON_OFF);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawTexture(this.x, this.y, 15, 15);

        this.image.updateTexture();
        GlStateManager.bindTexture(this.image.textureId);
        this.drawTexture(this.x + 2, this.y + 2, 11, 11);
    }

    public boolean isHovered(){
        return this.hovered;
    }

    private void drawTexture(int x, int y, int width, int height){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        int z = 0;
        bufferbuilder.pos(x, y + height, z).tex(1, 0).endVertex();
        bufferbuilder.pos(x + width, y + height, z).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, z).tex(0, 1).endVertex();
        bufferbuilder.pos(x, y, z).tex(0, 0).endVertex();
        tessellator.draw();
    }
}
