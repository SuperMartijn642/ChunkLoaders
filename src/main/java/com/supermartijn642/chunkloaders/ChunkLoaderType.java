package com.supermartijn642.chunkloaders;

import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.registry.RegistrationHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Created 22/02/2022 by SuperMartijn642
 */
public enum ChunkLoaderType {

    SINGLE(0, ChunkLoaderBlock.SINGLE_SHAPE, ChunkLoadersConfig.singleChunkLoaderRadius, false, "Single Chunk Loader"),
    BASIC(1, ChunkLoaderBlock.BASIC_SHAPE, ChunkLoadersConfig.basicChunkLoaderRadius, false, "Basic Chunk Loader"),
    ADVANCED(2, ChunkLoaderBlock.ADVANCED_SHAPE, ChunkLoadersConfig.advancedChunkLoaderRadius, true, "Advanced Chunk Loader"),
    ULTIMATE(3, ChunkLoaderBlock.ULTIMATE_SHAPE, ChunkLoadersConfig.ultimateChunkLoaderRadius, true, "Ultimate Chunk Loader");

    public static ChunkLoaderType byIndex(int index){
        for(ChunkLoaderType type : values()){
            if(type.index == index)
                return type;
        }
        return null;
    }

    private final int index;
    private final String registryName;
    private final BlockShape shape;
    private final Supplier<Integer> range;
    /**
     * Whether the hovering animation should rotate on all axis
     */
    private final boolean fullRotation;
    private final String englishTranslation;

    private BaseBlockEntityType<ChunkLoaderBlockEntity> blockEntityType;
    private ChunkLoaderBlock block;
    private BaseBlockItem item;

    ChunkLoaderType(int index, BlockShape shape, Supplier<Integer> range, boolean fullRotation, String englishTranslation){
        this.index = index;
        this.registryName = this.name().toLowerCase(Locale.ROOT) + "_chunk_loader";
        this.shape = shape;
        this.range = range;
        this.fullRotation = fullRotation;
        this.englishTranslation = englishTranslation;
    }

    public int getIndex(){
        return this.index;
    }

    public String getRegistryName(){
        return this.registryName;
    }

    public ChunkLoaderBlock getBlock(){
        return this.block;
    }

    public ChunkLoaderBlockEntity createBlockEntity(){
        return new ChunkLoaderBlockEntity(this);
    }

    public BaseBlockEntityType<ChunkLoaderBlockEntity> getBlockEntityType(){
        return this.blockEntityType;
    }

    public BaseBlockItem getItem(){
        return this.item;
    }

    public BlockShape getShape(){
        return this.shape;
    }

    public int getRange(){
        return this.range.get();
    }

    public int getGridSize(){
        return this.range.get() * 2 - 1;
    }

    /**
     * Whether the hovering animation of the chunk loader block should rotate on all axis
     */
    public boolean getFullRotation(){
        return this.fullRotation;
    }

    public String getEnglishTranslation(){
        return this.englishTranslation;
    }

    public void registerBlock(RegistrationHandler.Helper<Block> helper){
        if(this.block != null)
            throw new IllegalStateException("Blocks have already been registered!");

        this.block = new ChunkLoaderBlock(this);
        helper.register(this.registryName, this.block);
    }

    public void registerBlockEntity(RegistrationHandler.Helper<TileEntityType<?>> helper){
        if(this.blockEntityType != null)
            throw new IllegalStateException("Block entities have already been registered!");
        if(this.block == null)
            throw new IllegalStateException("Blocks must be registered before registering block entity types!");

        this.blockEntityType = BaseBlockEntityType.create(this::createBlockEntity, this.block);
        helper.register(this.registryName + "_tile", this.blockEntityType);
    }

    public void registerItem(RegistrationHandler.Helper<Item> helper){
        if(this.item != null)
            throw new IllegalStateException("Items have already been registered!");
        if(this.block == null)
            throw new IllegalStateException("Blocks must be registered before registering items!");

        this.item = new BaseBlockItem(this.block, ItemProperties.create().group(ChunkLoaders.GROUP));
        helper.register(this.registryName, this.item);
    }
}
