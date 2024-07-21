package com.supermartijn642.chunkloaders.generators;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersRecipeGenerator extends RecipeGenerator {

    public ChunkLoadersRecipeGenerator(ResourceCache cache){
        super("chunkloaders", cache);
    }

    @Override
    public void generate(){
        // Single chunk loader
        this.shapeless(ChunkLoaderType.SINGLE.getItem(), 9)
            .input(ChunkLoaderType.BASIC.getItem())
            .unlockedBy(ChunkLoaderType.BASIC.getItem());
        // Single chunk loader to basic chunk loader
        this.shaped("single_to_basic_chunk_loader", ChunkLoaderType.BASIC.getItem())
            .pattern("AAA")
            .pattern("AAA")
            .pattern("AAA")
            .input('A', ChunkLoaderType.SINGLE.getItem())
            .unlockedBy(ChunkLoaderType.SINGLE.getItem());
        // Basic chunk loader
        this.shaped(ChunkLoaderType.BASIC.getItem())
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.OBSIDIANS)
            .input('C', Tags.Items.ENDER_PEARLS)
            .unlockedBy(Tags.Items.ENDER_PEARLS);
        // Advanced chunk loader
        this.shaped(ChunkLoaderType.ADVANCED.getItem())
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', Items.BLAZE_POWDER)
            .input('B', Tags.Items.INGOTS_GOLD)
            .input('C', ChunkLoaderType.BASIC.getItem())
            .unlockedBy(ChunkLoaderType.BASIC.getItem());
        // Ultimate chunk loader
        this.shaped(ChunkLoaderType.ULTIMATE.getItem())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .input('A', Tags.Items.DUSTS_REDSTONE)
            .input('B', Tags.Items.GEMS_DIAMOND)
            .input('C', Items.ENDER_EYE)
            .input('D', ChunkLoaderType.ADVANCED.getItem())
            .unlockedBy(ChunkLoaderType.ADVANCED.getItem());
    }
}
