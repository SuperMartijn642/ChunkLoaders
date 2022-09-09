package com.supermartijn642.chunkloaders.generators;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;

/**
 * Created 10/07/2022 by SuperMartijn642
 */
public class ChunkLoadersTagGenerator extends TagGenerator {

    public ChunkLoadersTagGenerator(ResourceCache cache){
        super("chunkloaders", cache);
    }

    @Override
    public void generate(){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.blockMineableWithPickaxe().add(type.getBlock());
    }
}
