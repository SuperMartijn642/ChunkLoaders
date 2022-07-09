package com.supermartijn642.chunkloaders.data;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersItemModelProvider extends ItemModelProvider {

    public ChunkLoadersItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper){
        super(generator, "chunkloaders", existingFileHelper);
    }

    @Override
    protected void registerModels(){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.withExistingParent(type.getRegistryName(), new ResourceLocation("chunkloaders", "block/" + type.getRegistryName()));
    }

    @Override
    public String getName(){
        return "Chunk Loaders Item Model Provider";
    }
}
