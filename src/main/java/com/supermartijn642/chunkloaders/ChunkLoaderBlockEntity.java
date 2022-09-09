package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.block.BaseBlockEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import java.util.Random;
import java.util.UUID;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlockEntity extends BaseBlockEntity {

    private final ChunkLoaderType type;
    public final int animationOffset = new Random().nextInt(20000);

    private UUID owner;

    public ChunkLoaderBlockEntity(ChunkLoaderType type){
        super(type.getBlockEntityType());
        this.type = type;
    }

    public ChunkLoaderType getChunkLoaderType(){
        return this.type;
    }

    void setOwner(UUID owner){
        if(!this.world.isRemote && owner != null){
            this.owner = owner;
            this.dataChanged();
            ChunkLoadingCapability.get(this.world).castServer().addChunkLoader(this);
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
        if(!this.world.isRemote && this.owner != null)
            ChunkLoadingCapability.get(this.world).castServer().addChunkLoader(this);
    }

    @Override
    protected NBTTagCompound writeData(){
        NBTTagCompound compound = new NBTTagCompound();
        if(this.owner != null)
            compound.setUniqueId("owner", this.owner);
        return compound;
    }

    @Override
    protected void readData(NBTTagCompound compound){
        this.owner = compound.hasKey("ownerLeast", Constants.NBT.TAG_LONG) ? compound.getUniqueId("owner") : null;
    }
}
