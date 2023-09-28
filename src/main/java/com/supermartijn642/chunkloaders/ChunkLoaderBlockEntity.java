package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.block.BaseBlockEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
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

    @Override
    public void invalidate(){
        super.invalidate();
        // Add check to see if block gets removed 1 tick later, for cases like Create's contraptions
        if(!this.world.isRemote && this.world.getMinecraftServer() != null){
            IBlockState state = this.getBlockState();
            this.world.getMinecraftServer().addScheduledTask(() -> {
                Chunk chunk = this.world.getChunkProvider().getLoadedChunk(this.pos.getX() >> 4, this.pos.getY() >> 4);
                if(chunk != null && chunk.getBlockState(this.pos).getBlock() != state.getBlock() && !(chunk.getTileEntity(this.pos, Chunk.EnumCreateEntityType.CHECK) instanceof ChunkLoaderBlockEntity)){
                    if(this.hasOwner())
                        ChunkLoadingCapability.get(this.world).castServer().removeChunkLoader(this);
                    else{ // Remove from legacy capability
                        LegacyChunkLoadingCapability.ChunkTracker cap = this.world.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY, null);
                        if(cap != null)
                            cap.remove(this.pos);
                    }
                }
            });
        }
    }
}
