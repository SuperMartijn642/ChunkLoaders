package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkGrid {

    private static final ResourceLocation GRID_OVERLAY = new ResourceLocation("chunkloaders", "textures/gui/grid_overlay.png");

    private final int x, y;
    private final int width, height;
    private final int rows, columns;
    private final ChunkPos topLeftChunk;
    private final UUID player;

    private final List<ChunkGridCell> cells = new ArrayList<>();
    private final boolean[][] loadedChunks;
    private final boolean[][] withinRangeChunks;
    private final boolean[][] loadedChunksForOtherPlayers;

    private boolean doDrag = false;
    private boolean dragState = false;
    private final List<ChunkGridCell> draggedButtons = new ArrayList<>();

    public ChunkGrid(int x, int y, int rows, int columns, ChunkPos topLeftChunk, UUID player, int loaderYLevel){
        this.x = x;
        this.y = y;
        this.width = columns * 18 + 2;
        this.height = rows * 18 + 2;
        this.rows = rows;
        this.columns = columns;
        this.topLeftChunk = topLeftChunk;
        this.player = player;

        // Create all the cells
        for(int row = 0; row < rows; row++){
            for(int column = 0; column < columns; column++){
                int cellX = this.x + 1 + column * 18;
                int cellY = this.y + 1 + row * 18;
                ChunkPos pos = new ChunkPos(topLeftChunk.x + column, topLeftChunk.z + row);
                int finalRow = row, finalColumn = column;
                this.cells.add(new ChunkGridCell(cellX, cellY, pos, loaderYLevel, player,
                    (hOffset, vOffset) -> this.isChunkLoaded(finalRow + vOffset, finalColumn + hOffset),
                    (hOffset, vOffset) -> this.isChunkWithinRange(finalRow + vOffset, finalColumn + hOffset),
                    (hOffset, vOffset) -> this.isChunkLoadedByOtherPlayer(finalRow + vOffset, finalColumn + hOffset)));
            }
        }
        this.loadedChunks = new boolean[rows][columns];
        this.withinRangeChunks = new boolean[rows][columns];
        this.loadedChunksForOtherPlayers = new boolean[rows][columns];

        this.update();
    }

    public void renderOutline(PoseStack poseStack){
        ScreenUtils.fillRect(poseStack, this.x + 1, this.y, this.width - 2, 1, 0, 0, 0, 1);
        ScreenUtils.fillRect(poseStack, this.x, this.y + 1, this.width, this.height - 2, 0, 0, 0, 1);
        ScreenUtils.fillRect(poseStack, this.x + 1, this.y + this.height - 1, this.width - 2, 1, 0, 0, 0, 1);
        ScreenUtils.bindTexture(GRID_OVERLAY);
        ScreenUtils.drawTexture(poseStack, this.x + 1, this.y + 1, this.width - 2, this.height - 2, 0, 0, this.columns, this.rows);
    }

    public void renderForeground(PoseStack poseStack){
        this.cells.forEach(cell -> cell.drawHoverOutline(poseStack));
    }

    public void renderTooltips(Consumer<List<Component>> tooltipRenderer){
        this.cells.forEach(cell -> cell.renderTooltip(tooltipRenderer));
    }

    public List<ChunkGridCell> getCells(){
        return this.cells;
    }

    public void update(){
        for(int row = 0; row < this.rows; row++){
            for(int column = 0; column < this.columns; column++){
                ChunkPos pos = new ChunkPos(this.topLeftChunk.x + column, this.topLeftChunk.z + row);
                ChunkLoadingCapability capability = ChunkLoadingCapability.get(ClientUtils.getWorld());
                this.loadedChunks[row][column] = capability.isChunkLoadedByPlayer(this.player, pos);
                this.withinRangeChunks[row][column] = capability.canPlayerLoadChunk(this.player, pos);
                Set<UUID> activePlayers = capability.getActivePlayersLoadingChunk(pos);
                if(activePlayers.contains(this.player) ? activePlayers.size() > 1 : !activePlayers.isEmpty())
                    this.loadedChunksForOtherPlayers[row][column] = true;
                else{
                    Set<UUID> inactivePlayers = capability.getInactivePlayersLoadingChunk(pos);
                    this.loadedChunksForOtherPlayers[row][column] = inactivePlayers.contains(this.player) ? inactivePlayers.size() > 1 : !inactivePlayers.isEmpty();
                }
            }
        }
    }

    private boolean isChunkLoaded(int row, int column){
        if(row < 0 || row >= this.rows || column < 0 || column >= this.columns)
            return false;

        return this.loadedChunks[row][column];
    }

    private boolean isChunkWithinRange(int row, int column){
        if(row < 0 || row >= this.rows || column < 0 || column >= this.columns)
            return false;

        return this.withinRangeChunks[row][column] && !this.loadedChunks[row][column];
    }

    private boolean isChunkLoadedByOtherPlayer(int row, int column){
        if(row < 0 || row >= this.rows || column < 0 || column >= this.columns)
            return false;

        return this.loadedChunksForOtherPlayers[row][column];
    }

    public boolean mouseClicked(int button){
        if(button == 0){
            for(ChunkGridCell cell : this.cells){
                if(cell.isHovered()){
                    this.doDrag = true;
                    this.dragState = !cell.isLoaded();
                    this.draggedButtons.clear();
                    this.draggedButtons.add(cell);
                    cell.onPress();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseDragged(int button){
        if(this.doDrag && button == 0){
            for(ChunkGridCell cell : this.cells){
                if(cell.isHovered() && cell.isLoaded() != this.dragState && !this.draggedButtons.contains(cell)){
                    cell.onPress();
                    this.draggedButtons.add(cell);
                    return true;
                }
            }
        }
        return false;
    }

    public void mouseReleased(int button){
        if(button == 0)
            this.doDrag = false;
    }
}
