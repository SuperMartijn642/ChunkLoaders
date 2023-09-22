package com.supermartijn642.chunkloaders.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Created 20/02/2023 by SuperMartijn642
 */
public class ChunkLoadingCapabilitySaveData extends SavedData {

    private static final String IDENTIFIER = "chunkloaders_loaded_chunks";

    private final ChunkLoadingCapability capability;

    public static void init(ServerLevel level, ChunkLoadingCapability capability){
        level.getDataStorage().computeIfAbsent(new Factory<SavedData>(
            () -> new ChunkLoadingCapabilitySaveData(capability),
            tag -> {
                ChunkLoadingCapabilitySaveData saveData = new ChunkLoadingCapabilitySaveData(capability);
                saveData.load(tag);
                return saveData;
            },
            null
        ), IDENTIFIER);
    }

    public ChunkLoadingCapabilitySaveData(ChunkLoadingCapability capability){
        this.capability = capability;
    }

    @Override
    public CompoundTag save(CompoundTag tag){
        return this.capability.write();
    }

    public void load(CompoundTag tag){
        this.capability.read(tag);
    }

    @Override
    public boolean isDirty(){
        return true;
    }
}
