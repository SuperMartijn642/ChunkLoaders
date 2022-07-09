package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.block.BaseTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

import java.util.Random;
import java.util.UUID;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlockEntity extends BaseTileEntity {

    public static class SingleChunkLoaderBlockEntity extends ChunkLoaderBlockEntity {
        public SingleChunkLoaderBlockEntity(){
            super(ChunkLoaderType.SINGLE);
        }
    }

    public static class BasicChunkLoaderBlockEntity extends ChunkLoaderBlockEntity {
        public BasicChunkLoaderBlockEntity(){
            super(ChunkLoaderType.BASIC);
        }
    }

    public static class AdvancedChunkLoaderBlockEntity extends ChunkLoaderBlockEntity {
        public AdvancedChunkLoaderBlockEntity(){
            super(ChunkLoaderType.ADVANCED);
        }
    }

    public static class UltimateChunkLoaderBlockEntity extends ChunkLoaderBlockEntity {
        public UltimateChunkLoaderBlockEntity(){
            super(ChunkLoaderType.ULTIMATE);
        }
    }

    private final ChunkLoaderType type;
    public final int animationOffset = new Random().nextInt(20000);

    private UUID owner;

    public ChunkLoaderBlockEntity(ChunkLoaderType type){
        super(type.getTileEntityType());
        this.type = type;
    }

    public ChunkLoaderType getChunkLoaderType(){
        return this.type;
    }

    void setOwner(UUID owner){
        if(!this.level.isClientSide() && owner != null){
            this.owner = owner;
            this.dataChanged();
            ChunkLoadingCapability.get(this.level).castServer().addChunkLoader(this);
        }
    }

    public UUID getOwner(){
        return this.owner;
    }

    public boolean hasOwner(){
        return this.owner != null;
    }

    @Override
    public void onLoad(){
        if(!this.level.isClientSide() && this.owner != null)
            ChunkLoadingCapability.get(this.level).castServer().addChunkLoader(this);
    }

    @Override
    protected CompoundNBT writeData(){
        CompoundNBT compound = new CompoundNBT();
        if(this.owner != null)
            compound.putUUID("owner", this.owner);
        return compound;
    }

    @Override
    protected void readData(CompoundNBT compound){
        this.owner = compound.contains("ownerLeast", Constants.NBT.TAG_LONG) ? compound.getUUID("owner") : null;
    }
}
