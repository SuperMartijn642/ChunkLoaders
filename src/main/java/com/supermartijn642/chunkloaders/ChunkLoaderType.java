package com.supermartijn642.chunkloaders;

import com.supermartijn642.core.block.BlockShape;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

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

    private TileEntityType<ChunkLoaderBlockEntity> tileEntityType;
    private ChunkLoaderBlock block;
    private BlockItem item;

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

    public ChunkLoaderBlockEntity createTileEntity(){
        return new ChunkLoaderBlockEntity(this);
    }

    public TileEntityType<ChunkLoaderBlockEntity> getTileEntityType(){
        return this.tileEntityType;
    }

    public BlockItem getItem(){
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

    public void registerBlock(IForgeRegistry<Block> registry){
        if(this.block != null)
            throw new IllegalStateException("Blocks have already been registered!");

        this.block = new ChunkLoaderBlock(this);
        registry.register(this.block);
    }

    public void registerTileEntity(IForgeRegistry<TileEntityType<?>> registry){
        if(this.tileEntityType != null)
            throw new IllegalStateException("Tile entities have already been registered!");
        if(this.block == null)
            throw new IllegalStateException("Blocks must be registered before registering tile entity types!");

        this.tileEntityType = TileEntityType.Builder.of(this::createTileEntity, this.block).build(null);
        this.tileEntityType.setRegistryName(this.registryName + "_tile");
        registry.register(this.tileEntityType);
    }

    public void registerItem(IForgeRegistry<Item> registry){
        if(this.item != null)
            throw new IllegalStateException("Items have already been registered!");
        if(this.block == null)
            throw new IllegalStateException("Blocks must be registered before registering items!");

        this.item = new BlockItem(this.block, new Item.Properties().tab(ChunkLoaders.GROUP));
        this.item.setRegistryName(this.registryName);
        registry.register(this.item);
    }
}
