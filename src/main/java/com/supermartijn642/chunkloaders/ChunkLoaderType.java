package com.supermartijn642.chunkloaders;

import com.supermartijn642.core.block.BlockShape;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Created 22/02/2022 by SuperMartijn642
 */
public enum ChunkLoaderType {

    SINGLE(0, ChunkLoaderBlock.SINGLE_SHAPE, ChunkLoadersConfig.singleChunkLoaderRadius, false, "Single Chunk Loader", ChunkLoaderBlockEntity.SingleChunkLoaderBlockEntity.class, ChunkLoaderBlockEntity.SingleChunkLoaderBlockEntity::new),
    BASIC(1, ChunkLoaderBlock.BASIC_SHAPE, ChunkLoadersConfig.basicChunkLoaderRadius, false, "Basic Chunk Loader", ChunkLoaderBlockEntity.BasicChunkLoaderBlockEntity.class, ChunkLoaderBlockEntity.BasicChunkLoaderBlockEntity::new),
    ADVANCED(2, ChunkLoaderBlock.ADVANCED_SHAPE, ChunkLoadersConfig.advancedChunkLoaderRadius, true, "Advanced Chunk Loader", ChunkLoaderBlockEntity.AdvancedChunkLoaderBlockEntity.class, ChunkLoaderBlockEntity.AdvancedChunkLoaderBlockEntity::new),
    ULTIMATE(3, ChunkLoaderBlock.ULTIMATE_SHAPE, ChunkLoadersConfig.ultimateChunkLoaderRadius, true, "Ultimate Chunk Loader", ChunkLoaderBlockEntity.UltimateChunkLoaderBlockEntity.class, ChunkLoaderBlockEntity.UltimateChunkLoaderBlockEntity::new);

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
    private final Class<? extends ChunkLoaderBlockEntity> blockEntityClass;
    private final Supplier<? extends ChunkLoaderBlockEntity> blockEntitySupplier;

    private ChunkLoaderBlock block;
    private ItemBlock item;

    ChunkLoaderType(int index, BlockShape shape, Supplier<Integer> range, boolean fullRotation, String englishTranslation, Class<? extends ChunkLoaderBlockEntity> blockEntityClass, Supplier<? extends ChunkLoaderBlockEntity> blockEntitySupplier){
        this.index = index;
        this.registryName = this.name().toLowerCase(Locale.ROOT) + "_chunk_loader";
        this.shape = shape;
        this.range = range;
        this.fullRotation = fullRotation;
        this.englishTranslation = englishTranslation;
        this.blockEntityClass = blockEntityClass;
        this.blockEntitySupplier = blockEntitySupplier;
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
        return this.blockEntitySupplier.get();
    }

    public ItemBlock getItem(){
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

    public Class<? extends ChunkLoaderBlockEntity> getBlockEntityClass(){
        return this.blockEntityClass;
    }

    public void registerBlock(IForgeRegistry<Block> registry){
        if(this.block != null)
            throw new IllegalStateException("Blocks have already been registered!");

        this.block = new ChunkLoaderBlock(this);
        this.block.setCreativeTab(ChunkLoaders.GROUP);
        registry.register(this.block);
    }

    public void registerTileEntity(){
        if(this.block == null)
            throw new IllegalStateException("Blocks must be registered before registering tile entity types!");

        GameRegistry.registerTileEntity(this.blockEntityClass, new ResourceLocation("chunkloaders", this.registryName + "_tile"));
    }

    public void registerItem(IForgeRegistry<Item> registry){
        if(this.item != null)
            throw new IllegalStateException("Items have already been registered!");
        if(this.block == null)
            throw new IllegalStateException("Blocks must be registered before registering items!");

        this.item = new ItemBlock(this.block);
        this.item.setRegistryName(this.registryName);
        registry.register(this.item);
    }
}
