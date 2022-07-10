package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

/**
 * Created 22/02/2022 by SuperMartijn642
 */
public class ChunkLoaderCache {

    public final BlockPos chunkLoaderPos;
    public final ChunkPos chunkPos;
    public final ChunkLoaderType chunkLoaderType;
    public final UUID owner;

    public ChunkLoaderCache(BlockPos pos, ChunkLoaderType type, UUID owner){
        if(pos == null)
            throw new IllegalArgumentException("Chunk loader position must not be null!");
        if(type == null)
            throw new IllegalArgumentException("Chunk loader type must not be null!");
        if(owner == null)
            throw new IllegalArgumentException("Owner must not be null!");
        this.chunkLoaderPos = pos;
        this.chunkPos = new ChunkPos(pos);
        this.chunkLoaderType = type;
        this.owner = owner;
    }

    public CompoundTag write(){
        CompoundTag compound = new CompoundTag();
        compound.putLong("chunkLoaderPos", this.chunkLoaderPos.asLong());
        compound.putInt("chunkLoaderType", this.chunkLoaderType.getIndex());
        compound.putUUID("owner", this.owner);
        return compound;
    }

    public static ChunkLoaderCache read(CompoundTag compound){
        return new ChunkLoaderCache(BlockPos.of(compound.getLong("chunkLoaderPos")), ChunkLoaderType.byIndex(compound.getInt("chunkLoaderType")), compound.getUUID("owner"));
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass()) return false;

        ChunkLoaderCache that = (ChunkLoaderCache)o;

        if(!this.chunkLoaderPos.equals(that.chunkLoaderPos)) return false;
        if(!this.chunkPos.equals(that.chunkPos)) return false;
        if(this.chunkLoaderType != that.chunkLoaderType) return false;
        return this.owner.equals(that.owner);
    }

    @Override
    public int hashCode(){
        int result = this.chunkLoaderPos.hashCode();
        result = 31 * result + this.chunkPos.hashCode();
        result = 31 * result + this.chunkLoaderType.hashCode();
        result = 31 * result + this.owner.hashCode();
        return result;
    }
}
