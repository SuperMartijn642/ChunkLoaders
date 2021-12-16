package com.supermartijn642.chunkloaders;

import com.supermartijn642.core.block.BaseTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderTile extends BaseTileEntity {

    public final int animationOffset = new Random().nextInt(20000);

    private int gridSize;
    private int radius;
    private boolean[][] grid; // [x][z]

    private boolean dataChanged = false;

    public ChunkLoaderTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state, int gridSize){
        super(tileEntityTypeIn, pos, state);
        this.gridSize = gridSize;
        this.radius = (gridSize - 1) / 2;
        this.grid = new boolean[gridSize][gridSize];
    }

    public void unloadAll(){
        this.level.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.level.getChunk(this.worldPosition).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    if(this.grid[x][z])
                        tracker.remove(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.worldPosition);
                }
            }
        });
    }

    public void loadAll(){
        this.level.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.level.getChunk(this.worldPosition).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    this.grid[x][z] = true;
                    tracker.add(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.worldPosition);
                }
            }
        });
        this.dataChanged();
    }

    public void toggle(int xOffset, int zOffset){
        this.level.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.level.getChunk(this.worldPosition).getPos();
            if(this.grid[xOffset + radius][zOffset + radius])
                tracker.remove(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.worldPosition);
            else
                tracker.add(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.worldPosition);
            this.grid[xOffset + radius][zOffset + radius] = !this.grid[xOffset + radius][zOffset + radius];
        });
        this.dataChanged();
    }

    public boolean isLoaded(int xOffset, int zOffset){
        return this.grid[xOffset + radius][zOffset + radius];
    }

    public int getGridSize(){
        return this.gridSize;
    }

    public void dataChanged(){
        if(this.level.isClientSide)
            return;
        this.dataChanged = true;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag tag = new CompoundTag();
        tag.putInt("gridSize", this.gridSize);
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                tag.putBoolean(x + ";" + z, this.grid[x][z]);
            }
        }
        return tag;
    }

    @Override
    protected void readData(CompoundTag tag){
        this.gridSize = tag.contains("gridSize") ? tag.getInt("gridSize") : this.gridSize;
        if(this.gridSize < 1 || this.gridSize % 2 == 0)
            this.gridSize = 1;
        this.radius = (this.gridSize - 1) / 2;
        this.grid = new boolean[this.gridSize][this.gridSize];
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                this.grid[x][z] = tag.contains(x + ";" + z) && tag.getBoolean(x + ";" + z);
            }
        }
    }
}
