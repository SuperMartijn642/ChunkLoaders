package com.supermartijn642.chunkloaders.data;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

/**
 * Created 10/07/2022 by SuperMartijn642
 */
public class ChunkLoadersBlockTagsProvider extends BlockTagsProvider {

    public ChunkLoadersBlockTagsProvider(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper){
        super(p_126511_, "chunkloaders", existingFileHelper);
    }

    @Override
    protected void addTags(){
        TagAppender<Block> tag = this.tag(BlockTags.MINEABLE_WITH_PICKAXE);
        for(ChunkLoaderType type : ChunkLoaderType.values())
            tag.add(type.getBlock());
    }
}
