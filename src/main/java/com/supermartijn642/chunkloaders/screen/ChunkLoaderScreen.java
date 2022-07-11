package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.chunkloaders.ChunkLoadersConfig;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.BaseScreen;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public class ChunkLoaderScreen extends BaseScreen {

    private final ChunkPos pos;
    private final UUID chunkLoaderOwner;
    private final int mapYLevel;
    private final int mapWidth, mapHeight;

    private ChunkGrid grid;

    public ChunkLoaderScreen(ChunkPos centerPos, UUID chunkLoaderOwner, int mapYLevel, int mapWidth, int mapHeight){
        super(TextComponents.translation("chunkloaders.gui.title").get());
        if(mapWidth % 2 == 0 || mapHeight % 2 == 0)
            throw new IllegalArgumentException("Map width and height must be uneven!");
        this.pos = centerPos;
        this.chunkLoaderOwner = chunkLoaderOwner;
        this.mapYLevel = mapYLevel;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    @Override
    protected float sizeX(){
        return 14 + this.mapWidth * 18;
    }

    @Override
    protected float sizeY(){
        return 14 + this.mapHeight * 18;
    }

    @Override
    protected void addWidgets(){
        // Create a new chunk grid
        ChunkPos topLeftChunk = new ChunkPos(this.pos.x - (this.mapWidth - 1) / 2, this.pos.z - (this.mapHeight - 1) / 2);
        this.grid = new ChunkGrid(6, 6, this.mapHeight, this.mapWidth, topLeftChunk, this.chunkLoaderOwner, this.mapYLevel);
        // Add all the individual cell widgets to the screen
        this.grid.getCells().forEach(this::addWidget);
    }

    @Override
    public void tick(){
        this.grid.update();
        super.tick();
    }

    @Override
    protected void renderBackground(PoseStack poseStack, int mouseX, int mouseY){
        // Side panel
        String username = PlayerRenderer.getPlayerUsername(this.chunkLoaderOwner);
        int usernameWidth = username == null ? 0 : this.font.width(TextComponents.string(username).color(ChatFormatting.WHITE).get());
        this.drawScreenBackground(poseStack, this.sizeX() - 10, this.sizeY() / 2 - 30, Math.max(100, usernameWidth + 39), 60);
        // Center grid background
        this.drawScreenBackground(poseStack);
    }

    @Override
    protected void render(PoseStack poseStack, int mouseX, int mouseY){
        this.grid.renderOutline(poseStack);

        // Side panel
        float panelX = this.sizeX(), panelY = this.sizeY() / 2 - 30;
        // Owner
        ScreenUtils.drawString(poseStack, TextComponents.translation("chunkloaders.gui.owner").get(), panelX + 5, panelY + 7);
        PlayerRenderer.renderPlayerHead(this.chunkLoaderOwner, poseStack, (int)panelX + 5, (int)panelY + 18, 12, 12);
        String username = PlayerRenderer.getPlayerUsername(this.chunkLoaderOwner);
        if(username != null)
            ScreenUtils.drawStringWithShadow(poseStack, TextComponents.string(username).color(ChatFormatting.WHITE).get(), panelX + 21, panelY + 20);
        // Loaded chunks
        ScreenUtils.drawString(poseStack, TextComponents.translation("chunkloaders.gui.loaded_chunks").get(), panelX + 5, panelY + 33);
        int loadedCount = ChunkLoadingCapability.get(ClientUtils.getWorld()).getChunksLoadedByPlayer(this.chunkLoaderOwner).size();
        int maxLoaded = ChunkLoadersConfig.maxLoadedChunksPerPlayer.get();
        TextComponents.TextComponentBuilder loadedText = maxLoaded > 0 ?
            TextComponents.translation("chunkloaders.gui.loaded_chunks.count_max", loadedCount, maxLoaded).color(loadedCount < maxLoaded ? ChatFormatting.WHITE : ChatFormatting.RED) :
            TextComponents.translation("chunkloaders.gui.loaded_chunks.count", loadedCount).color(ChatFormatting.WHITE);
        ScreenUtils.drawStringWithShadow(poseStack, loadedText.get(), panelX + 5, panelY + 44);
    }

    @Override
    protected void renderForeground(PoseStack poseStack, int mouseX, int mouseY){
        this.grid.renderForeground(poseStack);
    }

    @Override
    protected void renderTooltips(PoseStack PoseStack, int mouseX, int mouseY){
        Consumer<List<Component>> tooltipRenderer = tooltips -> this.renderComponentTooltip(PoseStack, tooltips, mouseX, mouseY);
        this.grid.renderTooltips(tooltipRenderer);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        return this.grid.mouseClicked(button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
        return this.grid.mouseDragged(button);
    }

    @Override
    protected void onMouseRelease(int mouseX, int mouseY, int button){
        this.grid.mouseReleased(button);
    }
}
