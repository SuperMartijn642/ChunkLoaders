package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.block.BaseBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.SectionPos;
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
        if(!this.level.isClientSide && owner != null){
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
        if(!this.level.isClientSide && this.owner != null)
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

    @Override
    public void setRemoved(){
        super.setRemoved();
        // Add check to see if block gets removed 1 tick later, for cases like Create's contraptions
        if(!this.level.isClientSide && this.level.getServer() != null){
            BlockState state = this.getBlockState();
            this.level.getServer().tell(new TickDelayedTask(1, () -> {
                Chunk chunk = this.level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(this.worldPosition.getX()), SectionPos.blockToSectionCoord(this.worldPosition.getY()));
                if(chunk != null && chunk.getBlockState(this.worldPosition).getBlock() != state.getBlock() && !(chunk.getBlockEntity(this.worldPosition) instanceof ChunkLoaderBlockEntity)){
                    if(this.hasOwner())
                        ChunkLoadingCapability.get(this.level).castServer().removeChunkLoader(this);
                    else // Remove from legacy capability
                        this.level.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY).ifPresent(cap -> cap.remove(this.worldPosition));
                }
            }));
        }
    }
}
