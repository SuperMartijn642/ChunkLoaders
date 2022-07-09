package com.supermartijn642.chunkloaders.data;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersBlockStateProvider extends BlockStateProvider {

    public ChunkLoadersBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper){
        super(gen, "chunkloaders", exFileHelper);
    }

    @Override
    protected void registerStatesAndModels(){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.getVariantBuilder(type.getBlock()).forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(this.getExistingFile(new ResourceLocation("chunkloaders", "block/" + type.getRegistryName())))});
    }
}
