package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.block.BaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.TickTask;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Random;
import java.util.UUID;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlockEntity extends BaseBlockEntity {

    private final ChunkLoaderType type;
    public final int animationOffset = new Random().nextInt(20000);

    private UUID owner;

    public ChunkLoaderBlockEntity(BlockPos pos, BlockState state, ChunkLoaderType type){
        super(type.getBlockEntityType(), pos, state);
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

    public void onLoad(){
        if(!this.level.isClientSide && this.owner != null)
            ChunkLoadingCapability.get(this.level).castServer().addChunkLoader(this);
    }

    @Override
    protected CompoundTag writeData(){
        CompoundTag compound = new CompoundTag();
        if(this.owner != null)
            compound.putUUID("owner", this.owner);
        return compound;
    }

    @Override
    protected void readData(CompoundTag compound){
        this.owner = compound.contains("owner", Tag.TAG_INT_ARRAY) ? compound.getUUID("owner") : null;
    }

    @Override
    public void setRemoved(){
        super.setRemoved();
        // Add check to see if block gets removed 1 tick later, for cases like Create's contraptions
        if(!this.level.isClientSide && this.level.getServer() != null){
            BlockState state = this.getBlockState();
            this.level.getServer().tell(new TickTask(1, () -> {
                LevelChunk chunk = this.level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(this.worldPosition.getX()), SectionPos.blockToSectionCoord(this.worldPosition.getY()));
                if(chunk != null && chunk.getBlockState(this.worldPosition).getBlock() != state.getBlock() && !(chunk.getBlockEntity(this.worldPosition) instanceof ChunkLoaderBlockEntity)){
                    if(this.hasOwner())
                        ChunkLoadingCapability.get(this.level).castServer().removeChunkLoader(this);
                }
            }));
        }
    }
}
