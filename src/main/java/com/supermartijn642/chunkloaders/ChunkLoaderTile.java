package com.supermartijn642.chunkloaders;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderTile extends TileEntity {

    public static class SingleChunkLoaderTile extends ChunkLoaderTile {
        public SingleChunkLoaderTile(){
            super(ChunkLoadersConfig.singleChunkLoaderRadius.get() * 2 - 1);
        }
    }

    public static class BasicChunkLoaderTile extends ChunkLoaderTile {
        public BasicChunkLoaderTile(){
            super(ChunkLoadersConfig.basicChunkLoaderRadius.get() * 2 - 1);
        }
    }

    public static class AdvancedChunkLoaderTile extends ChunkLoaderTile {
        public AdvancedChunkLoaderTile(){
            super(ChunkLoadersConfig.advancedChunkLoaderRadius.get() * 2 - 1);
        }
    }

    public static class UltimateChunkLoaderTile extends ChunkLoaderTile {
        public UltimateChunkLoaderTile(){
            super(ChunkLoadersConfig.ultimateChunkLoaderRadius.get() * 2 - 1);
        }
    }


    public final int animationOffset = new Random().nextInt(20000);

    private int gridSize;
    private int radius;
    private boolean[][] grid; // [x][z]

    private boolean dataChanged = false;

    public ChunkLoaderTile(int gridSize){
        super();
        this.gridSize = gridSize;
        this.radius = (gridSize - 1) / 2;
        this.grid = new boolean[gridSize][gridSize];
    }

    public void unloadAll(){
        ChunkLoaderUtil.ChunkTracker tracker = this.world.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY, null);
        if(tracker != null){
            ChunkPos pos = this.world.getChunkFromBlockCoords(this.pos).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    if(this.grid[x][z])
                        tracker.remove(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.pos);
                }
            }
        }
    }

    public void loadAll(){
        ChunkLoaderUtil.ChunkTracker tracker = this.world.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY, null);
        if(tracker != null){
            ChunkPos pos = this.world.getChunkFromBlockCoords(this.pos).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    this.grid[x][z] = true;
                    tracker.add(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.pos);
                }
            }
        }
        this.dataChanged();
    }

    public void toggle(int xOffset, int zOffset){
        ChunkLoaderUtil.ChunkTracker tracker = this.world.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY, null);
        if(tracker != null){
            ChunkPos pos = this.world.getChunkFromBlockCoords(this.pos).getPos();
            if(this.grid[xOffset + radius][zOffset + radius])
                tracker.remove(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.pos);
            else
                tracker.add(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.pos);
            this.grid[xOffset + radius][zOffset + radius] = !this.grid[xOffset + radius][zOffset + radius];
        }
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

    private NBTTagCompound getChangedData(){
        if(this.dataChanged){
            this.dataChanged = false;
            return this.getData();
        }
        return null;
    }

    private NBTTagCompound getData(){
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("gridSize", this.gridSize);
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                tag.setBoolean(x + ";" + z, this.grid[x][z]);
            }
        }
        return tag;
    }

    private void handleData(NBTTagCompound tag){
        this.gridSize = tag.hasKey("gridSize") ? tag.getInteger("gridSize") :
            this instanceof SingleChunkLoaderTile ? 1 :
                this instanceof BasicChunkLoaderTile ? 3 :
                    this instanceof AdvancedChunkLoaderTile ? 5 :
                        this instanceof UltimateChunkLoaderTile ? 7 : -1;
        if(this.gridSize < 1 || this.gridSize % 2 == 0)
            this.gridSize = 1;
        this.radius = (this.gridSize - 1) / 2;
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                this.grid[x][z] = tag.hasKey(x + ";" + z) && tag.getBoolean(x + ";" + z);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound){
        super.writeToNBT(compound);
        compound.setTag("data", this.getData());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound){
        super.readFromNBT(compound);
        this.handleData(compound.getCompoundTag("data"));
    }

    @Override
    public NBTTagCompound getUpdateTag(){
        NBTTagCompound tag = super.getUpdateTag();
        tag.setTag("data", this.getData());
        return tag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag){
        super.handleUpdateTag(tag);
        this.handleData(tag.getCompoundTag("data"));
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket(){
        NBTTagCompound tag = this.getChangedData();
        return tag == null || tag.hasNoTags() ? null : new SPacketUpdateTileEntity(this.pos, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
        this.handleData(pkt.getNbtCompound());
    }

    private IBlockState getBlockState(){
        return this.world.getBlockState(this.pos);
    }
}
