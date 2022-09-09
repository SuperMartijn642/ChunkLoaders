package com.supermartijn642.chunkloaders.generators;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.core.generator.ModelGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import net.minecraft.util.ResourceLocation;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersModelGenerator extends ModelGenerator {

    public ChunkLoadersModelGenerator(ResourceCache cache){
        super("chunkloaders", cache);
    }

    @Override
    public void generate(){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.model("item/" + type.getRegistryName()).parent(new ResourceLocation("chunkloaders", "block/" + type.getRegistryName()));
    }
}
