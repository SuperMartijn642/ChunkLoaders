package com.supermartijn642.chunkloaders.generators;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersLootTableGenerator extends LootTableGenerator {

    public ChunkLoadersLootTableGenerator(ResourceCache cache){
        super("chunkloaders", cache);
    }

    @Override
    public void generate(){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.dropSelf(type.getBlock());
    }
}
