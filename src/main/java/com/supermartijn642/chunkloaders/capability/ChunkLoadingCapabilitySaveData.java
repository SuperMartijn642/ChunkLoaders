package com.supermartijn642.chunkloaders.capability;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Created 20/02/2023 by SuperMartijn642
 */
public class ChunkLoadingCapabilitySaveData extends SavedData {

    private static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("chunkloaders", "loaded_chunks");

    private final ChunkLoadingCapability capability;

    public static void init(ServerLevel level, ChunkLoadingCapability capability){
        level.getDataStorage().computeIfAbsent(new SavedDataType<>(
            IDENTIFIER,
            () -> new ChunkLoadingCapabilitySaveData(capability),
            new Codec<>() {
                @Override
                public <T> DataResult<Pair<ChunkLoadingCapabilitySaveData,T>> decode(DynamicOps<T> ops, T input){
                    try{
                        ChunkLoadingCapabilitySaveData saveData = new ChunkLoadingCapabilitySaveData(capability);
                        saveData.load((CompoundTag)ops.convertTo(NbtOps.INSTANCE, input));
                        return DataResult.success(Pair.of(saveData, input));
                    }catch(Exception e){
                        return DataResult.error(e::getMessage);
                    }
                }

                @Override
                public <T> DataResult<T> encode(ChunkLoadingCapabilitySaveData input, DynamicOps<T> ops, T prefix){
                    try{
                        return DataResult.success(NbtOps.INSTANCE.convertTo(ops, input.save()));
                    }catch(Exception e){
                        return DataResult.error(e::getMessage);
                    }
                }
            }
        ));
    }

    public ChunkLoadingCapabilitySaveData(ChunkLoadingCapability capability){
        this.capability = capability;
    }

    public CompoundTag save(){
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
