package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketToggleChunk;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import com.supermartijn642.core.gui.widget.IHoverTextWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.function.Supplier;

/**
 * Created 9/2/2020 by SuperMartijn642
 */
public class ChunkButton extends AbstractButtonWidget implements IHoverTextWidget {

    private static final ResourceLocation BUTTON_OFF = new ResourceLocation("chunkloaders", "textures/gui/button_off.png"),
        BUTTON_ON = new ResourceLocation("chunkloaders", "textures/gui/button_on.png");

    public final int xOffset, zOffset;
    private final Supplier<ChunkLoaderTile> tileSupplier;
    private final ChunkImage image;

    public ChunkButton(int x, int y, int xOffset, int zOffset, Supplier<ChunkLoaderTile> tileSupplier, ChunkPos chunk){
        super(x, y, 15, 15, () -> ChunkLoaders.CHANNEL.sendToServer(new PacketToggleChunk(tileSupplier.get().getBlockPos(), xOffset, zOffset)));
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.tileSupplier = tileSupplier;
        this.image = new ChunkImage(ClientUtils.getWorld(), chunk);
        this.image.createTexture();
    }

    @Override
    protected Component getNarrationMessage(){
        return TextComponents.translation(this.isLoaded() ? "chunkloaders.gui.narrate_loaded" : "chunkloaders.gui.narrate_unloaded").get();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
        ScreenUtils.bindTexture(this.isLoaded() ? BUTTON_ON : BUTTON_OFF);
        ScreenUtils.drawTexture(matrixStack, this.x, this.y, 15, 15);

        this.image.updateTexture();
        RenderSystem.setShaderTexture(0, this.image.textureId);
        ScreenUtils.drawTexture(matrixStack, this.x + 2, this.y + 2, 11, 11);

        if(!this.isLoaded())
            ScreenUtils.fillRect(matrixStack, this.x + 2, this.y + 2, 11, 11, 0xaa000000);
    }

    public boolean isLoaded(){
        ChunkLoaderTile tile = this.tileSupplier.get();
        return tile != null && tile.isLoaded(this.xOffset, this.zOffset);
    }

    @Override
    public Component getHoverText(){
        return TextComponents.translation(this.isLoaded() ? "chunkloaders.gui.loaded" : "chunkloaders.gui.unloaded").get();
    }
}
