package com.supermartijn642.chunkloaders.generators;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersBlockStateGenerator extends BlockStateGenerator {

    public ChunkLoadersBlockStateGenerator(ResourceCache cache){
        super("chunkloaders", cache);
    }

    @Override
    public void generate(){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.blockState(type.getBlock()).emptyVariant(builder -> builder.model("block/" + type.getRegistryName()));
    }
}
