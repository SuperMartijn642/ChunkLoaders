package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.packet.PacketToggleChunk;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created 9/2/2020 by SuperMartijn642
 */
public class ChunkGridCell extends BaseWidget {

    public static final ResourceLocation CELL_OVERLAY = ResourceLocation.fromNamespaceAndPath("chunkloaders", "gui/cell_overlay");
    public static final ResourceLocation GRID_OVERLAY = ResourceLocation.fromNamespaceAndPath("chunkloaders", "gui/grid_overlay");

    private final ChunkPos pos;
    private final UUID player;
    private final BiFunction<Integer,Integer,Boolean> isLoaded;
    private final BiFunction<Integer,Integer,Boolean> isWithinRange;
    private final BiFunction<Integer,Integer,Boolean> isLoadedByOtherPlayer;
    private final ChunkImage image;

    public ChunkGridCell(int x, int y, ChunkPos chunk, int loaderYLevel, UUID player, BiFunction<Integer,Integer,Boolean> isLoaded, BiFunction<Integer,Integer,Boolean> isWithinRange, BiFunction<Integer,Integer,Boolean> isLoadedByOtherPlayer){
        super(x, y, 18, 18);
        this.pos = chunk;
        this.player = player;
        this.isLoaded = isLoaded;
        this.isWithinRange = isWithinRange;
        this.isLoadedByOtherPlayer = isLoadedByOtherPlayer;
        this.image = new ChunkImage(ClientUtils.getWorld(), chunk, loaderYLevel);
    }

    @Override
    public Component getNarrationMessage(){
        if(this.isLoaded.apply(0, 0))
            return TextComponents.translation("chunkloaders.gui.chunk.loaded").get();
        if(this.isWithinRange.apply(0, 0))
            return TextComponents.translation("chunkloaders.gui.chunk.available").get();
        if(this.isLoadedByOtherPlayer.apply(0, 0))
            return TextComponents.translation("chunkloaders.gui.speech.chunk.others").get();
        return TextComponents.translation("chunkloaders.gui.speech.chunk.not_loaded").get();
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitSprite(GRID_OVERLAY, this.x, this.y, this.width, this.height);
        graphics.submitTexture(this.image.getTexture(), this.x + 1, this.y + 1, 16, 16);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        // Draw chunks claimed by others
        this.drawOutline(graphics, this.isLoadedByOtherPlayer, 40, 40, 40, 196, 196, 196, 97);

        // Draw chunks within range
        this.drawOutline(graphics, this.isWithinRange, 0, 82, 196, 0, 82, 196, 58);

        // Draw claimed chunks
        this.drawOutline(graphics, this.isLoaded, 0, 99, 11, 0, 99, 11, 116);
    }

    @Override
    public void renderForeground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        if(this.isFocused() && this.canPlayerToggleChunk())
            graphics.submitSprite(CELL_OVERLAY, this.x - 1, this.y - 1, this.width + 2, this.height + 2);
    }

    private void drawOutline(GuiGraphicsHelper graphics, BiFunction<Integer,Integer,Boolean> shouldConnect, int redBorder, int greenBorder, int blueBorder, int redFiller, int greenFiller, int blueFiller, int alphaFiller){
        if(!shouldConnect.apply(0, 0))
            return;

        graphics.submitRectangle(this.x, this.y, this.width, this.height, p -> p.color(redFiller, greenFiller, blueFiller, alphaFiller));

        // Top
        if(!shouldConnect.apply(0, -1))
            graphics.submitRectangle(this.x, this.y, this.width, 1, p -> p.color(redBorder, greenBorder, blueBorder, 1));
        // Right
        if(!shouldConnect.apply(1, 0))
            graphics.submitRectangle(this.x + this.width - 1, this.y, 1, this.height, p -> p.color(redBorder, greenBorder, blueBorder, 1));
        // Bottom
        if(!shouldConnect.apply(0, 1))
            graphics.submitRectangle(this.x, this.y + this.height - 1, this.width, 1, p -> p.color(redBorder, greenBorder, blueBorder, 1));
        // Left
        if(!shouldConnect.apply(-1, 0))
            graphics.submitRectangle(this.x, this.y, 1, this.height, p -> p.color(redBorder, greenBorder, blueBorder, 1));

        // Top-left
        if(shouldConnect.apply(0, -1) && shouldConnect.apply(-1, 0) && !shouldConnect.apply(-1, -1))
            graphics.submitRectangle(this.x, this.y, 1, 1, p -> p.color(redBorder, greenBorder, blueBorder, 1));
        // Top-right
        if(shouldConnect.apply(0, -1) && shouldConnect.apply(1, 0) && !shouldConnect.apply(1, -1))
            graphics.submitRectangle(this.x + this.width - 1, this.y, 1, 1, p -> p.color(redBorder, greenBorder, blueBorder, 1));
        // Bottom-left
        if(shouldConnect.apply(0, 1) && shouldConnect.apply(-1, 0) && !shouldConnect.apply(-1, 1))
            graphics.submitRectangle(this.x, this.y + this.height - 1, 1, 1, p -> p.color(redBorder, greenBorder, blueBorder, 1));
        // Bottom-right
        if(shouldConnect.apply(0, 1) && shouldConnect.apply(1, 0) && !shouldConnect.apply(1, 1))
            graphics.submitRectangle(this.x + this.width - 1, this.y + this.height - 1, 1, 1, p -> p.color(redBorder, greenBorder, blueBorder, 1));
    }

    @Override
    protected void getTooltips(Consumer<Component> tooltipConsumer){
        if(this.isFocused()){
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
            if(!ClientUtils.getPlayer().getUUID().equals(this.player) && ClientUtils.getPlayer().hasPermissions(2) && !Minecraft.getInstance().hasShiftDown()
                && (this.isWithinRange.apply(0, 0) || this.isLoaded.apply(0, 0))){
                Component keyName = TextComponents.translation("key.keyboard.left.shift").color(ChatFormatting.GOLD).get();
                tooltips.add(TextComponents.translation("chunkloaders.gui.chunk.overwrite", keyName).color(ChatFormatting.WHITE).get());
            }
            tooltips.forEach(tooltipConsumer);
        }
    }

    public void onPress(){
        if(this.canPlayerToggleChunk()){
            AbstractButtonWidget.playClickSound();
            ChunkLoaders.CHANNEL.sendToServer(new PacketToggleChunk(this.player, this.pos));
        }
    }

    private boolean canPlayerToggleChunk(){
        Player player = ClientUtils.getPlayer();
        return (player.getUUID().equals(this.player) || (player.hasPermissions(2) && Minecraft.getInstance().hasShiftDown()))
            && (this.isWithinRange.apply(0, 0) || this.isLoaded.apply(0, 0));
    }

    public boolean isLoaded(){
        return this.isLoaded.apply(0, 0);
    }

    @Override
    public void discard(){
        this.image.dispose();
    }

    public static void drawTexture(PoseStack poseStack, VertexConsumer buffer, float x, float y, float width, float height){
        Matrix4f matrix = poseStack.last().pose();
        buffer.addVertex(matrix, x, y + height, 0).setUv(0, 1).setColor(1, 1, 1, 1f);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(1, 1).setColor(1, 1, 1, 1f);
        buffer.addVertex(matrix, x + width, y, 0).setUv(1, 0).setColor(1, 1, 1, 1f);
        buffer.addVertex(matrix, x, y, 0).setUv(0, 0).setColor(1, 1, 1, 1f);
    }
}
