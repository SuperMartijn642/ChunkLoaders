package com.supermartijn642.chunkloaders.generators;

import com.supermartijn642.chunkloaders.screen.ChunkGridCell;
import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 08/07/2025 by SuperMartijn642
 */
public class ChunkLoadersAtlasSourceGenerator extends AtlasSourceGenerator {

    public ChunkLoadersAtlasSourceGenerator(ResourceCache cache){
        super("chunkloaders", cache);
    }

    @Override
    public void generate(){
        this.guiAtlas()
            .texture(ChunkGridCell.GRID_OVERLAY)
            .texture(ChunkGridCell.CELL_OVERLAY);
    }
}
