package com.supermartijn642.chunkloaders.data;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersLanguageProvider extends LanguageProvider {

    public ChunkLoadersLanguageProvider(DataGenerator gen){
        super(gen, "chunkloaders", "en_us");
    }

    @Override
    protected void addTranslations(){
        // Chunk Loaders' creative tab
        this.add("itemGroup.chunkloaders", "Chunk Loaders");
        // Chunk loader tooltips
        this.add("chunkloaders.chunk_loader.info.single", "Can load a single chunk");
        this.add("chunkloaders.chunk_loader.info.multiple", "Can load chunks in a %1$d by %1$d chunk area");
        // Key binds
        this.add("chunkloaders.keys.category", "Chunk Loaders");
        this.add("chunkloaders.keys.open_screen", "View Chunk Loaders");
        // Chunk loader gui
        this.add("chunkloaders.gui.title", "Chunk Loader Screen");
        this.add("chunkloaders.gui.loaded", "LOADED");
        this.add("chunkloaders.gui.unloaded", "NOT LOADED");
        this.add("chunkloaders.gui.owner", "Owner:");
        this.add("chunkloaders.gui.loaded_chunks", "Loaded chunks:");
        this.add("chunkloaders.gui.loaded_chunks.count", "%s");
        this.add("chunkloaders.gui.loaded_chunks.count_max", "%1$s / %2$s");
        this.add("chunkloaders.gui.chunk.loaded", "Loaded");
        this.add("chunkloaders.gui.chunk.available", "Click to load");
        this.add("chunkloaders.gui.chunk.others", "Loaded by");
        this.add("chunkloaders.gui.chunk.overwrite", "Hold %s to overwrite");
        this.add("chunkloaders.gui.speech.chunk.others", "Loaded by other players");
        this.add("chunkloaders.gui.speech.chunk.not_loaded", "Not loaded");
        this.add("chunkloaders.gui.disabled", "The chunk loader map is disabled on this server");
        // Legacy messages
        this.add("chunkloaders.legacy_message", "Please shift-right-click this chunk loader to set its owner!");
        this.add("chunkloaders.legacy_success", "You are now the owner of this chunk loader!");

        // Add translations for the chunk loader blocks
        for(ChunkLoaderType type : ChunkLoaderType.values())
            this.add(type.getBlock(), type.getEnglishTranslation());
    }
}
