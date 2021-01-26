package com.supermartijn642.chunkloaders;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderTile extends TileEntity {

    public final int animationOffset = new Random().nextInt(20000);

    private int gridSize;
    private int radius;
    private boolean[][] grid; // [x][z]

    private boolean dataChanged = false;

    public ChunkLoaderTile(TileEntityType<?> tileEntityTypeIn, int gridSize){
        super(tileEntityTypeIn);
        this.gridSize = gridSize;
        this.radius = (gridSize - 1) / 2;
        this.grid = new boolean[gridSize][gridSize];
    }

    public void unloadAll(){
        this.world.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.world.getChunk(this.pos).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    if(this.grid[x][z])
                        tracker.remove(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.pos);
                }
            }
        });
    }

    public void loadAll(){
        this.world.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.world.getChunk(this.pos).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    this.grid[x][z] = true;
                    tracker.add(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.pos);
                }
            }
        });
        this.dataChanged();
    }

    public void toggle(int xOffset, int zOffset){
        this.world.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.world.getChunk(this.pos).getPos();
            if(this.grid[xOffset + radius][zOffset + radius])
                tracker.remove(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.pos);
            else
                tracker.add(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.pos);
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
        if(this.world.isRemote)
            return;
        this.dataChanged = true;
        this.markDirty();
        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 2);
    }

    private CompoundNBT getChangedData(){
        if(this.dataChanged){
            this.dataChanged = false;
            return this.getData();
        }
        return null;
    }

    private CompoundNBT getData(){
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("gridSize", this.gridSize);
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                tag.putBoolean(x + ";" + z, this.grid[x][z]);
            }
        }
        return tag;
    }

    private void handleData(CompoundNBT tag){
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

    @Override
    public CompoundNBT write(CompoundNBT compound){
        super.write(compound);
        compound.put("data", this.getData());
        return compound;
    }

    @Override
    public void read(CompoundNBT compound){
        super.read(compound);
        this.handleData(compound.getCompound("data"));
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT tag = super.getUpdateTag();
        tag.put("data", this.getData());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag){
        super.handleUpdateTag(tag);
        this.handleData(tag.getCompound("data"));
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        CompoundNBT tag = this.getChangedData();
        return tag == null || tag.isEmpty() ? null : new SUpdateTileEntityPacket(this.pos, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.handleData(pkt.getNbtCompound());
    }
}
