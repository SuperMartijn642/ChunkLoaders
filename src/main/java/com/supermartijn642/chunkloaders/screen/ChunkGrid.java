package com.supermartijn642.chunkloaders.screen;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.Widget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkGrid extends BaseWidget {

    private static final ResourceLocation GRID_OVERLAY = new ResourceLocation("chunkloaders", "textures/gui/grid_overlay.png");

    private final int rows, columns;
    private final ChunkPos topLeftChunk;
    private final UUID player;
    private final int loaderYLevel;

    private final List<ChunkGridCell> cells = new ArrayList<>();
    private final boolean[][] loadedChunks;
    private final boolean[][] withinRangeChunks;
    private final boolean[][] loadedChunksForOtherPlayers;

    private boolean doDrag = false;
    private boolean dragState = false;
    private final List<ChunkGridCell> draggedButtons = new ArrayList<>();

    public ChunkGrid(int x, int y, int rows, int columns, ChunkPos topLeftChunk, UUID player, int loaderYLevel){
        super(x, y, columns * 18 + 2, rows * 18 + 2);
        this.rows = rows;
        this.columns = columns;
        this.topLeftChunk = topLeftChunk;
        this.player = player;
        this.loaderYLevel = loaderYLevel;

        this.loadedChunks = new boolean[rows][columns];
        this.withinRangeChunks = new boolean[rows][columns];
        this.loadedChunksForOtherPlayers = new boolean[rows][columns];

        this.update();
    }

    @Override
    protected void addWidgets(){
        // Create all the cells
        for(int row = 0; row < this.rows; row++){
            for(int column = 0; column < this.columns; column++){
                int cellX = this.x + 1 + column * 18;
                int cellY = this.y + 1 + row * 18;
                ChunkPos pos = new ChunkPos(this.topLeftChunk.x + column, this.topLeftChunk.z + row);
                int finalRow = row, finalColumn = column;
                this.addWidget(new ChunkGridCell(cellX, cellY, pos, this.loaderYLevel, this.player,
                    (hOffset, vOffset) -> this.isChunkLoaded(finalRow + vOffset, finalColumn + hOffset),
                    (hOffset, vOffset) -> this.isChunkWithinRange(finalRow + vOffset, finalColumn + hOffset),
                    (hOffset, vOffset) -> this.isChunkLoadedByOtherPlayer(finalRow + vOffset, finalColumn + hOffset)));
            }
        }
    }

    @Override
    protected <T extends Widget> T addWidget(T widget){
        if(widget instanceof ChunkGridCell)
            this.cells.add((ChunkGridCell)widget);
        return super.addWidget(widget);
    }

    @Override
    public void renderBackground(int mouseX, int mouseY){
        ScreenUtils.fillRect(this.x + 1, this.y, this.width - 2, 1, 0, 0, 0, 1);
        ScreenUtils.fillRect(this.x, this.y + 1, this.width, this.height - 2, 0, 0, 0, 1);
        ScreenUtils.fillRect(this.x + 1, this.y + this.height - 1, this.width - 2, 1, 0, 0, 0, 1);
        ScreenUtils.bindTexture(GRID_OVERLAY);
        ScreenUtils.drawTexture(this.x + 1, this.y + 1, this.width - 2, this.height - 2, 0, 0, this.columns, this.rows);

        super.renderBackground(mouseX, mouseY);
    }

    @Override
    public ITextComponent getNarrationMessage(){
        return null;
    }

    @Override
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

        super.update();
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

    @Override
    public boolean mousePressed(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(button == 0){
            for(ChunkGridCell cell : this.cells){
                if(cell.isFocused()){
                    this.doDrag = true;
                    this.dragState = !cell.isLoaded();
                    this.draggedButtons.clear();
                    this.draggedButtons.add(cell);
                    cell.onPress();
                    return true;
                }
            }
        }
        return super.mousePressed(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button, boolean hasBeenHandled){
        if(button == 0)
            this.doDrag = false;
        return super.mouseReleased(mouseX, mouseY, button, hasBeenHandled);
    }

    @Override
    public void render(int mouseX, int mouseY){
        super.render(mouseX, mouseY);

        // Update dragged cells
        if(this.doDrag){
            for(ChunkGridCell cell : this.cells){
                if(cell.isFocused() && cell.isLoaded() != this.dragState && !this.draggedButtons.contains(cell)){
                    cell.onPress();
                    this.draggedButtons.add(cell);
                }
            }
        }
    }
}
