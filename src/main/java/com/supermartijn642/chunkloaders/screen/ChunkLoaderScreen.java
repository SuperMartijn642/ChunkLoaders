package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.supermartijn642.chunkloaders.ChunkLoadersConfig;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public class ChunkLoaderScreen extends BaseWidget {

    private final ChunkPos pos;
    private final UUID chunkLoaderOwner;
    private final int mapYLevel;
    private final int mapWidth, mapHeight;

    public ChunkLoaderScreen(ChunkPos centerPos, UUID chunkLoaderOwner, int mapYLevel, int mapWidth, int mapHeight){
        super(0, 0, 14 + mapWidth * 18, 14 + mapHeight * 18);
        if(mapWidth % 2 == 0 || mapHeight % 2 == 0)
            throw new IllegalArgumentException("Map width and height must be uneven!");
        this.pos = centerPos;
        this.chunkLoaderOwner = chunkLoaderOwner;
        this.mapYLevel = mapYLevel;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    @Override
    protected void addWidgets(){
        // Create a new chunk grid
        ChunkPos topLeftChunk = new ChunkPos(this.pos.x - (this.mapWidth - 1) / 2, this.pos.z - (this.mapHeight - 1) / 2);
        this.addWidget(new ChunkGrid(6, 6, this.mapHeight, this.mapWidth, topLeftChunk, this.chunkLoaderOwner, this.mapYLevel));
    }

    @Override
    public ITextComponent getNarrationMessage(){
        return TextComponents.translation("chunkloaders.gui.title").get();
    }

    @Override
    public void renderBackground(MatrixStack poseStack, int mouseX, int mouseY){
        // Side panel
        String username = PlayerRenderer.getPlayerUsername(this.chunkLoaderOwner);
        int usernameWidth = username == null ? 0 : ClientUtils.getFontRenderer().width(TextComponents.string(username).color(TextFormatting.WHITE).get());
        int ownerHintWidth = ClientUtils.getFontRenderer().width(TextComponents.translation("chunkloaders.gui.owner").get());
        int loadedChunksHintWidth = ClientUtils.getFontRenderer().width(TextComponents.translation("chunkloaders.gui.loaded_chunks").get());
        int sidePanelWidth = 22 + Math.max(39 + usernameWidth, Math.max(ownerHintWidth, loadedChunksHintWidth));
        ScreenUtils.drawScreenBackground(poseStack, this.width - 10, this.height / 2f - 30, sidePanelWidth, 60);
        // Center grid background
        ScreenUtils.drawScreenBackground(poseStack, 0, 0, this.width, this.height);

        super.renderBackground(poseStack, mouseX, mouseY);
    }

    @Override
    public void renderForeground(MatrixStack poseStack, int mouseX, int mouseY){
        super.renderForeground(poseStack, mouseX, mouseY);
        // Side panel
        float panelX = this.width, panelY = this.height / 2f - 30;
        // Owner
        ScreenUtils.drawString(poseStack, TextComponents.translation("chunkloaders.gui.owner").get(), panelX + 5, panelY + 7);
        PlayerRenderer.renderPlayerHead(this.chunkLoaderOwner, poseStack, (int)panelX + 5, (int)panelY + 18, 12, 12);
        String username = PlayerRenderer.getPlayerUsername(this.chunkLoaderOwner);
        if(username != null)
            ScreenUtils.drawStringWithShadow(poseStack, TextComponents.string(username).color(TextFormatting.WHITE).get(), panelX + 21, panelY + 20);
        // Loaded chunks
        ScreenUtils.drawString(poseStack, TextComponents.translation("chunkloaders.gui.loaded_chunks").get(), panelX + 5, panelY + 33);
        int loadedCount = ChunkLoadingCapability.get(ClientUtils.getWorld()).getChunksLoadedByPlayer(this.chunkLoaderOwner).size();
        int maxLoaded = ChunkLoadersConfig.maxLoadedChunksPerPlayer.get();
        TextComponents.TextComponentBuilder loadedText = maxLoaded > 0 ?
            TextComponents.translation("chunkloaders.gui.loaded_chunks.count_max", loadedCount, maxLoaded).color(loadedCount < maxLoaded ? TextFormatting.WHITE : TextFormatting.RED) :
            TextComponents.translation("chunkloaders.gui.loaded_chunks.count", loadedCount).color(TextFormatting.WHITE);
        ScreenUtils.drawStringWithShadow(poseStack, loadedText.get(), panelX + 5, panelY + 44);
    }
}
