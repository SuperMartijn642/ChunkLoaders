package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketToggleChunk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * Created 9/2/2020 by SuperMartijn642
 */
public class ChunkButton extends AbstractButton {

    private static final ResourceLocation BUTTON_OFF = new ResourceLocation("chunkloaders", "textures/gui/button_off.png"),
        BUTTON_ON = new ResourceLocation("chunkloaders", "textures/gui/button_on.png");

    public final int xOffset, zOffset;
    private final Supplier<ChunkLoaderTile> tileSupplier;
    private final ChunkImage image;

    public ChunkButton(int x, int y, int xOffset, int zOffset, Supplier<ChunkLoaderTile> tileSupplier, World world, ChunkPos chunk){
        super(x, y, 15, 15, "");
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.tileSupplier = tileSupplier;
        this.image = new ChunkImage(world, chunk);
        this.image.createTexture();
    }

    @Override
    public void onPress(){
        ChunkLoaderTile tile = this.tileSupplier.get();
        if(tile != null)
            ChunkLoaders.CHANNEL.sendToServer(new PacketToggleChunk(tile.getPos(), this.xOffset, this.zOffset));
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks){
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(this.isLoaded() ? BUTTON_ON : BUTTON_OFF);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        drawTexture(this.x, this.y, 15, 15);

        this.image.updateTexture();
        GlStateManager.enableTexture();
        GlStateManager.bindTexture(this.image.textureId);
        this.drawTexture(this.x + 2, this.y + 2, 11, 11);

        if(!this.isLoaded())
            fillGradient(this.x + 2, this.y + 2, this.x + 13, this.y + 13, 0xaa000000, 0xaa000000);

        this.renderBg(minecraft, mouseX, mouseY);
    }

    public boolean isLoaded(){
        ChunkLoaderTile tile = this.tileSupplier.get();
        return tile != null && tile.isLoaded(this.xOffset, this.zOffset);
    }

    private void drawTexture(int x, int y, int width, int height){
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        int z = this.getBlitOffset();
        bufferbuilder.pos(x, y + height, z).tex(1, 0).endVertex();
        bufferbuilder.pos(x + width, y + height, z).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, z).tex(0, 1).endVertex();
        bufferbuilder.pos(x, y, z).tex(0, 0).endVertex();
        bufferbuilder.finishDrawing();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }
}
