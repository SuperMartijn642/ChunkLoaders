package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.packet.PacketToggleChunk;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.AbstractButtonWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created 9/2/2020 by SuperMartijn642
 */
public class ChunkGridCell extends AbstractButtonWidget {

    private static final ResourceLocation CELL_OVERLAY = new ResourceLocation("chunkloaders", "textures/gui/cell_overlay.png");

    private final ChunkPos pos;
    private final UUID player;
    private final BiFunction<Integer,Integer,Boolean> isLoaded;
    private final BiFunction<Integer,Integer,Boolean> isWithinRange;
    private final BiFunction<Integer,Integer,Boolean> isLoadedByOtherPlayer;
    private final ChunkImage image;

    public ChunkGridCell(int x, int y, ChunkPos chunk, int loaderYLevel, UUID player, BiFunction<Integer,Integer,Boolean> isLoaded, BiFunction<Integer,Integer,Boolean> isWithinRange, BiFunction<Integer,Integer,Boolean> isLoadedByOtherPlayer){
        super(x, y, 18, 18, null);
        this.pos = chunk;
        this.player = player;
        this.isLoaded = isLoaded;
        this.isWithinRange = isWithinRange;
        this.isLoadedByOtherPlayer = isLoadedByOtherPlayer;
        this.image = new ChunkImage(ClientUtils.getWorld(), chunk, loaderYLevel);
    }

    @Override
    protected Component getNarrationMessage(){
        if(this.isLoaded.apply(0, 0))
            return TextComponents.translation("chunkloaders.gui.chunk.loaded").get();
        if(this.isWithinRange.apply(0, 0))
            return TextComponents.translation("chunkloaders.gui.chunk.available").get();
        if(this.isLoadedByOtherPlayer.apply(0, 0))
            return TextComponents.translation("chunkloaders.gui.speech.chunk.others").get();
        return TextComponents.translation("chunkloaders.gui.speech.chunk.not_loaded").get();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks){
        this.image.bindTexture();
        ScreenUtils.drawTexture(poseStack, this.x + 1, this.y + 1, 16, 16);

        // Draw chunks claimed by others
        this.drawOutline(poseStack, this.isLoadedByOtherPlayer, 40 / 255f, 40 / 255f, 40 / 255f, 196 / 255f, 196 / 255f, 196 / 255f, 97 / 255f);

        // Draw chunks within range
        this.drawOutline(poseStack, this.isWithinRange, 0 / 255f, 82 / 255f, 196 / 255f, 0 / 255f, 82 / 255f, 196 / 255f, 58 / 255f);

        // Draw claimed chunks
        this.drawOutline(poseStack, this.isLoaded, 0 / 255f, 99 / 255f, 11 / 255f, 0 / 255f, 99 / 255f, 11 / 255f, 116 / 255f);
    }

    public void drawHoverOutline(PoseStack poseStack){
        if(this.isHovered() && this.canPlayerToggleChunk()){
            ScreenUtils.bindTexture(CELL_OVERLAY);
            ScreenUtils.drawTexture(poseStack, this.x - 1, this.y - 1, this.width + 2, this.height + 2);
        }
    }

    private void drawOutline(PoseStack poseStack, BiFunction<Integer,Integer,Boolean> shouldConnect, float redBorder, float greenBorder, float blueBorder, float redFiller, float greenFiller, float blueFiller, float alphaFiller){
        if(!shouldConnect.apply(0, 0))
            return;

        ScreenUtils.fillRect(poseStack, this.x, this.y, this.width, this.height, redFiller, greenFiller, blueFiller, alphaFiller);

        // Top
        if(!shouldConnect.apply(0, -1))
            ScreenUtils.fillRect(poseStack, this.x, this.y, this.width, 1, redBorder, greenBorder, blueBorder, 1);
        // Right
        if(!shouldConnect.apply(1, 0))
            ScreenUtils.fillRect(poseStack, this.x + this.width - 1, this.y, 1, this.height, redBorder, greenBorder, blueBorder, 1);
        // Bottom
        if(!shouldConnect.apply(0, 1))
            ScreenUtils.fillRect(poseStack, this.x, this.y + this.height - 1, this.width, 1, redBorder, greenBorder, blueBorder, 1);
        // Left
        if(!shouldConnect.apply(-1, 0))
            ScreenUtils.fillRect(poseStack, this.x, this.y, 1, this.height, redBorder, greenBorder, blueBorder, 1);

        // Top-left
        if(shouldConnect.apply(0, -1) && shouldConnect.apply(-1, 0) && !shouldConnect.apply(-1, -1))
            ScreenUtils.fillRect(poseStack, this.x, this.y, 1, 1, redBorder, greenBorder, blueBorder, 1);
        // Top-right
        if(shouldConnect.apply(0, -1) && shouldConnect.apply(1, 0) && !shouldConnect.apply(1, -1))
            ScreenUtils.fillRect(poseStack, this.x + this.width - 1, this.y, 1, 1, redBorder, greenBorder, blueBorder, 1);
        // Bottom-left
        if(shouldConnect.apply(0, 1) && shouldConnect.apply(-1, 0) && !shouldConnect.apply(-1, 1))
            ScreenUtils.fillRect(poseStack, this.x, this.y + this.height - 1, 1, 1, redBorder, greenBorder, blueBorder, 1);
        // Bottom-right
        if(shouldConnect.apply(0, 1) && shouldConnect.apply(1, 0) && !shouldConnect.apply(1, 1))
            ScreenUtils.fillRect(poseStack, this.x + this.width - 1, this.y + this.height - 1, 1, 1, redBorder, greenBorder, blueBorder, 1);
    }

    public void renderTooltip(Consumer<List<Component>> tooltipRenderer){
        if(this.isHovered()){
            List<Component> tooltips = new ArrayList<>();
            boolean canToggleChunk = this.canPlayerToggleChunk();
            if(canToggleChunk){
                if(this.isLoaded.apply(0, 0))
                    tooltips.add(TextComponents.translation("chunkloaders.gui.chunk.loaded").color(ChatFormatting.GOLD).get());
                else if(this.isWithinRange.apply(0, 0))
                    tooltips.add(TextComponents.translation("chunkloaders.gui.chunk.available").color(ChatFormatting.GOLD).get());
            }
            ChunkLoadingCapability capability = ChunkLoadingCapability.get(ClientUtils.getWorld());
            capability.getActivePlayersLoadingChunk(this.pos)
                .stream()
                .filter(uuid -> !canToggleChunk || !uuid.equals(this.player))
                .map(PlayerRenderer::getPlayerUsername)
                .filter(Objects::nonNull)
                .map(name -> TextComponents.string(" " + name).color(ChatFormatting.GRAY).italic().get())
                .forEach(tooltips::add);
            capability.getInactivePlayersLoadingChunk(this.pos)
                .stream()
                .filter(uuid -> !canToggleChunk || !uuid.equals(this.player))
                .map(PlayerRenderer::getPlayerUsername)
                .filter(Objects::nonNull)
                .map(name -> TextComponents.string(" ").string(name).color(ChatFormatting.GRAY).italic().strikethrough().get())
                .forEach(tooltips::add);
            if(tooltips.size() > (canToggleChunk ? 1 : 0))
                tooltips.add(canToggleChunk ? 1 : 0, TextComponents.translation("chunkloaders.gui.chunk.others").color(ChatFormatting.WHITE).get());
            if(!ClientUtils.getPlayer().getUUID().equals(this.player) && ClientUtils.getPlayer().hasPermissions(2) && !Screen.hasShiftDown()
                && (this.isWithinRange.apply(0, 0) || this.isLoaded.apply(0, 0))){
                Component keyName = TextComponents.fromTextComponent(ClientUtils.getMinecraft().options.keyShift.getTranslatedKeyMessage()).color(ChatFormatting.GOLD).get();
                tooltips.add(TextComponents.translation("chunkloaders.gui.chunk.overwrite", keyName).color(ChatFormatting.WHITE).get());
            }
            tooltipRenderer.accept(tooltips);
        }
    }

    @Override
    public void onPress(){
        if(this.canPlayerToggleChunk()){
            super.onPress();
            ChunkLoaders.CHANNEL.sendToServer(new PacketToggleChunk(this.player, this.pos));
        }
    }

    private boolean canPlayerToggleChunk(){
        Player player = ClientUtils.getPlayer();
        return (player.getUUID().equals(this.player) || (player.hasPermissions(2) && Screen.hasShiftDown()))
            && (this.isWithinRange.apply(0, 0) || this.isLoaded.apply(0, 0));
    }

    public boolean isLoaded(){
        return this.isLoaded.apply(0, 0);
    }

    public void dispose(){
        this.image.dispose();
    }
}
