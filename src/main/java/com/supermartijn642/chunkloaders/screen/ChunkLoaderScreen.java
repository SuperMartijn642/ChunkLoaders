package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public class ChunkLoaderScreen extends Screen {

    protected World world;
    protected BlockPos pos;
    protected int left, top;

    private final ResourceLocation background;
    private final int backgroundSize;

    private boolean doDrag = false;
    private boolean dragState = false;
    private List<ChunkButton> draggedButtons = new ArrayList<>();

    public ChunkLoaderScreen(String type, World world, BlockPos pos, int backgroundSize){
        super(new TranslationTextComponent("block.chunkloaders." + type));
        this.world = world;
        this.pos = pos;
        this.background = new ResourceLocation("chunkloaders", "textures/gui/" + type + ".png");
        this.backgroundSize = backgroundSize;
    }

    @Override
    protected void init(){
        ChunkLoaderTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        this.left = (this.width - this.backgroundSize) / 2;
        this.top = (this.height - this.backgroundSize) / 2;

        int radius = (tile.getGridSize() - 1) / 2;
        for(int x = 0; x < tile.getGridSize(); x++){
            for(int y = 0; y < tile.getGridSize(); y++){
                this.addButton(new ChunkButton(this.left + 8 + x * 16, this.top + 8 + y * 16, -radius + x, -radius + y,
                    this::getTileOrClose, this.world, new ChunkPos((pos.getX() >> 4) - radius + x, (pos.getZ() >> 4) - radius + y)));
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks){
        this.renderBackground();
        this.drawBackgroundLayer(partialTicks, mouseX, mouseY);
        super.render(mouseX, mouseY, partialTicks);

        ChunkLoaderTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        this.buttons.stream().filter(ChunkButton.class::isInstance).map(ChunkButton.class::cast).forEach(button -> {
            if(button.isHovered())
                this.renderToolTip(true, "chunkloaders.gui." + (tile.isLoaded(button.xOffset, button.zOffset) ? "loaded" : "unloaded"), mouseX, mouseY);
        });
    }

    private void drawBackgroundLayer(float partialTicks, int mouseX, int mouseY){
        ChunkLoaderTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getInstance().getTextureManager().bindTexture(this.background);
        this.drawTexture(this.left, this.top, this.backgroundSize, this.backgroundSize);
    }

    protected void drawCenteredString(ITextComponent text, float x, float y){
        this.drawCenteredString(text.getFormattedText(), x, y);
    }

    protected void drawCenteredString(String s, float x, float y){
        this.font.drawString(s, this.left + x - this.font.getStringWidth(s) / 2f, this.top + y, 4210752);
    }

    protected void drawString(ITextComponent text, float x, float y){
        this.drawString(text.getFormattedText(), x, y);
    }

    protected void drawString(String s, float x, float y){
        this.font.drawString(s, this.left + x, this.top + y, 4210752);
    }

    public void renderToolTip(boolean translate, String string, int x, int y){
        super.renderTooltip(translate ? new TranslationTextComponent(string).getFormattedText() : string, x, y);
    }

    public ChunkLoaderTile getTileOrClose(){
        if(this.world != null && this.pos != null){
            TileEntity tile = this.world.getTileEntity(this.pos);
            if(tile instanceof ChunkLoaderTile)
                return (ChunkLoaderTile)tile;
        }
        Minecraft.getInstance().player.closeScreen();
        return null;
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(button == 0){
            for(IGuiEventListener listener : this.children){
                if(listener instanceof ChunkButton){
                    ChunkButton chunkButton = (ChunkButton)listener;
                    if(chunkButton.isMouseOver(mouseX, mouseY)){
                        this.doDrag = true;
                        this.dragState = !chunkButton.isLoaded();
                        this.draggedButtons.clear();
                        this.draggedButtons.add(chunkButton);
                        chunkButton.onPress();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
        if(this.doDrag && button == 0){
            for(IGuiEventListener listener : this.children){
                if(listener instanceof ChunkButton && !this.draggedButtons.contains(listener)){
                    ChunkButton chunkButton = (ChunkButton)listener;
                    if(chunkButton.isMouseOver(mouseX, mouseY) && chunkButton.isLoaded() != this.dragState){
                        chunkButton.onPress();
                        this.draggedButtons.add(chunkButton);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if(button == 0)
            this.doDrag = false;
        return false;
    }

    private void drawTexture(int x, int y, int width, int height){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        int z = this.blitOffset;
        bufferbuilder.pos(x, y + height, z).tex(1, 0).endVertex();
        bufferbuilder.pos(x + width, y + height, z).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, z).tex(0, 1).endVertex();
        bufferbuilder.pos(x, y, z).tex(0, 0).endVertex();
        tessellator.draw();
    }
}
