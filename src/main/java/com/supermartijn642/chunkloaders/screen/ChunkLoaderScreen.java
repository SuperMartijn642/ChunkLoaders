package com.supermartijn642.chunkloaders.screen;

import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public class ChunkLoaderScreen extends GuiScreen {

    protected World world;
    protected BlockPos pos;
    protected int left, top;

    private final ResourceLocation background;
    private final int backgroundSize;

    private boolean doDrag = false;
    private boolean dragState = false;
    private List<ChunkButton> draggedButtons = new ArrayList<>();

    public ChunkLoaderScreen(String type, World world, BlockPos pos, int backgroundSize){
        this.world = world;
        this.pos = pos;
        this.background = new ResourceLocation("chunkloaders", "textures/gui/" + type + ".png");
        this.backgroundSize = backgroundSize;
    }

    @Override
    public void initGui(){
        ChunkLoaderTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        this.left = (this.width - this.backgroundSize) / 2;
        this.top = (this.height - this.backgroundSize) / 2;

        int radius = (tile.getGridSize() - 1) / 2;
        for(int x = 0; x < tile.getGridSize(); x++){
            for(int y = 0; y < tile.getGridSize(); y++){
                int index = x * tile.getGridSize() + y;
                this.addButton(new ChunkButton(index, this.left + 8 + x * 16, this.top + 8 + y * 16, -radius + x, -radius + y,
                    this::getTileOrClose, this.world, new ChunkPos((pos.getX() >> 4) - radius + x, (pos.getZ() >> 4) - radius + y)));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        this.drawBackgroundLayer(partialTicks, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);

        ChunkLoaderTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        this.buttonList.stream().filter(ChunkButton.class::isInstance).map(ChunkButton.class::cast).forEach(button -> {
            if(button.isHovered())
                this.renderToolTip(true, "chunkloaders.gui." + (tile.isLoaded(button.xOffset, button.zOffset) ? "loaded" : "unloaded"), mouseX, mouseY);
        });
    }

    private void drawBackgroundLayer(float partialTicks, int mouseX, int mouseY){
        ChunkLoaderTile tile = this.getTileOrClose();
        if(tile == null)
            return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(this.background);
        this.drawTexture(this.left, this.top, this.backgroundSize, this.backgroundSize);
    }

    protected void drawCenteredString(ITextComponent text, float x, float y){
        this.drawCenteredString(text.getFormattedText(), x, y);
    }

    protected void drawCenteredString(String s, float x, float y){
        this.fontRenderer.drawString(s, (int)(this.left + x - this.fontRenderer.getStringWidth(s) / 2f), (int)(this.top + y), 4210752);
    }

    protected void drawString(ITextComponent text, float x, float y){
        this.drawString(text.getFormattedText(), x, y);
    }

    protected void drawString(String s, float x, float y){
        this.fontRenderer.drawString(s, (int)(this.left + x), (int)(this.top + y), 4210752);
    }

    public void renderToolTip(boolean translate, String string, int x, int y){
        super.drawHoveringText(translate ? new TextComponentTranslation(string).getFormattedText() : string, x, y);
    }

    public ChunkLoaderTile getTileOrClose(){
        if(this.world != null && this.pos != null){
            TileEntity tile = this.world.getTileEntity(this.pos);
            if(tile instanceof ChunkLoaderTile)
                return (ChunkLoaderTile)tile;
        }
        Minecraft.getMinecraft().player.closeScreen();
        return null;
    }

    @Override
    public boolean doesGuiPauseGame(){
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if(button == 0){
            for(GuiButton listener : this.buttonList){
                if(listener instanceof ChunkButton){
                    ChunkButton chunkButton = (ChunkButton)listener;
                    if(chunkButton.isMouseOver(mouseX, mouseY)){
                        this.doDrag = true;
                        this.dragState = !chunkButton.isLoaded();
                        this.draggedButtons.clear();
                        this.draggedButtons.add(chunkButton);
                        chunkButton.onPress();
                        return;
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick){
        if(this.doDrag && button == 0){
            for(GuiButton listener : this.buttonList){
                if(listener instanceof ChunkButton && !this.draggedButtons.contains(listener)){
                    ChunkButton chunkButton = (ChunkButton)listener;
                    if(chunkButton.isMouseOver(mouseX, mouseY) && chunkButton.isLoaded() != this.dragState){
                        chunkButton.onPress();
                        this.draggedButtons.add(chunkButton);
                        return;
                    }
                }
            }
        }

        super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button){
        if(button == 0)
            this.doDrag = false;
    }

    @Override
    protected void actionPerformed(GuiButton button){
        if(button instanceof ChunkButton)
            ((ChunkButton)button).onPress();
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
