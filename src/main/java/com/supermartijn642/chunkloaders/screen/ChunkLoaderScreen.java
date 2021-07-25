package com.supermartijn642.chunkloaders.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.TileEntityBaseScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public class ChunkLoaderScreen extends TileEntityBaseScreen<ChunkLoaderTile> {

    private final int backgroundSize;

    private final List<ChunkButton> buttons = new LinkedList<>();
    private boolean doDrag = false;
    private boolean dragState = false;
    private List<ChunkButton> draggedButtons = new ArrayList<>();

    public ChunkLoaderScreen(String type, BlockPos pos){
        super(TextComponents.translation("block.chunkloaders." + type).get(), pos);
        ChunkLoaderTile tile = this.getObjectOrClose();
        int gridSize = tile == null ? 1 : tile.getGridSize();
        this.backgroundSize = gridSize * 15 + (gridSize - 1) + 16;
    }

    @Override
    protected float sizeX(@Nonnull ChunkLoaderTile blockEntity){
        return this.backgroundSize;
    }

    @Override
    protected float sizeY(@Nonnull ChunkLoaderTile blockEntity){
        return this.backgroundSize;
    }

    @Override
    protected void addWidgets(@Nonnull ChunkLoaderTile blockEntity){
        this.buttons.clear();
        int radius = (blockEntity.getGridSize() - 1) / 2;
        for(int x = 0; x < blockEntity.getGridSize(); x++){
            for(int y = 0; y < blockEntity.getGridSize(); y++){
                this.buttons.add(this.addWidget(new ChunkButton(8 + x * 16, 8 + y * 16, -radius + x, -radius + y,
                    this::getObjectOrClose, new ChunkPos((this.tilePos.getX() >> 4) - radius + x, (this.tilePos.getZ() >> 4) - radius + y))));
            }
        }
    }

    @Override
    protected void render(PoseStack matrixStack, int mouseX, int mouseY, @Nonnull ChunkLoaderTile blockEntity){
        this.drawScreenBackground(matrixStack);
    }

    @Override
    protected void onMousePress(int mouseX, int mouseY, int mouseButton){
        if(mouseButton == 0){
            for(ChunkButton button : this.buttons){
                if(button.isHovered()){
                    this.doDrag = true;
                    this.dragState = !button.isLoaded();
                    this.draggedButtons.clear();
                    this.draggedButtons.add(button);
                    return;
                }
            }
        }
    }

    @Override
    protected void onMouseRelease(int mouseX, int mouseY, int button){
        if(button == 0)
            this.doDrag = false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY){
        if(this.doDrag && mouseButton == 0){
            for(ChunkButton button : this.buttons){
                if(!this.draggedButtons.contains(button) && button.isHovered() && button.isLoaded() != this.dragState){
                    button.onPress();
                    this.draggedButtons.add(button);
                    return true;
                }
            }
        }
        return false;
    }
}
