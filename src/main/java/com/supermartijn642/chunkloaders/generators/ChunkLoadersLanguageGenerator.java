package com.supermartijn642.chunkloaders.generators;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersLanguageGenerator extends LanguageGenerator {

    public ChunkLoadersLanguageGenerator(ResourceCache cache){
        super("chunkloaders", cache, "en_us");
    }

    @Override
    public void generate(){
        // Chunk Loaders' creative tab
        this.itemGroup(ChunkLoaders.GROUP, "Chunk Loaders");
        // Chunk loader tooltips
        this.translation("chunkloaders.chunk_loader.info.single", "Can load a single chunk");
        this.translation("chunkloaders.chunk_loader.info.multiple", "Can load chunks in a %1$d by %1$d chunk area");
        // Key binds
        this.translation("chunkloaders.keys.category", "Chunk Loaders");
        this.translation("chunkloaders.keys.open_screen", "View Chunk Loaders");
        // Chunk loader gui
        this.translation("chunkloaders.gui.title", "Chunk Loader Screen");
        this.translation("chunkloaders.gui.loaded", "LOADED");
        this.translation("chunkloaders.gui.unloaded", "NOT LOADED");
        this.translation("chunkloaders.gui.owner", "Owner:");
        this.translation("chunkloaders.gui.loaded_chunks", "Loaded chunks:");
        this.translation("chunkloaders.gui.loaded_chunks.count", "%s");
        this.translation("chunkloaders.gui.loaded_chunks.count_max", "%1$s / %2$s");
        this.translation("chunkloaders.gui.chunk.loaded", "Loaded");
        this.translation("chunkloaders.gui.chunk.available", "Click to load");
        this.translation("chunkloaders.gui.chunk.others", "Loaded by");
        this.translation("chunkloaders.gui.chunk.overwrite", "Hold %s to overwrite");
        this.translation("chunkloaders.gui.speech.chunk.others", "Loaded by other players");
        this.translation("chunkloaders.gui.speech.chunk.not_loaded", "Not loaded");
        this.translation("chunkloaders.gui.disabled", "The chunk loader map is disabled on this server");
        // Legacy messages
        this.translation("chunkloaders.legacy_message", "Please shift-right-click this chunk loader to set its owner!");
        this.translation("chunkloaders.legacy_success", "You are now the owner of this chunk loader!");

        // Add translations for the chunk loader blocks
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.block(type.getBlock(), type.getEnglishTranslation());
    }
}
