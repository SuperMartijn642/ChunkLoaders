package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.packet.PacketToggleChunk;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = ChunkLoaders.MODID, name = ChunkLoaders.NAME, version = ChunkLoaders.VERSION, dependencies = ChunkLoaders.DEPENDENCIES)
public class ChunkLoaders {

    public static final String MODID = "chunkloaders";
    public static final String NAME = "Chunk Loaders";
    public static final String VERSION = "1.1.7";
    public static final String DEPENDENCIES = "required-after:supermartijn642configlib@[1.0.5,)";

    public static ChunkLoaders instance;

    public static SimpleNetworkWrapper channel;

    @GameRegistry.ObjectHolder("chunkloaders:single_chunk_loader")
    public static Block single_chunk_loader;
    @GameRegistry.ObjectHolder("chunkloaders:basic_chunk_loader")
    public static Block basic_chunk_loader;
    @GameRegistry.ObjectHolder("chunkloaders:advanced_chunk_loader")
    public static Block advanced_chunk_loader;
    @GameRegistry.ObjectHolder("chunkloaders:ultimate_chunk_loader")
    public static Block ultimate_chunk_loader;

    public ChunkLoaders(){
        instance = this;

        channel = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

        channel.registerMessage(PacketToggleChunk.class, PacketToggleChunk.class, 0, Side.SERVER);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        ChunkLoaderUtil.register();
    }

    @Mod.EventBusSubscriber
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            e.getRegistry().register(new ChunkLoaderBlock("single_chunk_loader", ChunkLoaderBlock.SINGLE_SHAPE, ChunkLoaderTile.SingleChunkLoaderTile::new, ChunkLoadersConfig.singleChunkLoaderRadius.get() * 2 - 1));
            e.getRegistry().register(new ChunkLoaderBlock("basic_chunk_loader", ChunkLoaderBlock.BASIC_SHAPE, ChunkLoaderTile.BasicChunkLoaderTile::new, ChunkLoadersConfig.basicChunkLoaderRadius.get() * 2 - 1));
            e.getRegistry().register(new ChunkLoaderBlock("advanced_chunk_loader", ChunkLoaderBlock.ADVANCED_SHAPE, ChunkLoaderTile.AdvancedChunkLoaderTile::new, ChunkLoadersConfig.advancedChunkLoaderRadius.get() * 2 - 1));
            e.getRegistry().register(new ChunkLoaderBlock("ultimate_chunk_loader", ChunkLoaderBlock.ULTIMATE_SHAPE, ChunkLoaderTile.UltimateChunkLoaderTile::new, ChunkLoadersConfig.ultimateChunkLoaderRadius.get() * 2 - 1));
            GameRegistry.registerTileEntity(ChunkLoaderTile.SingleChunkLoaderTile.class, new ResourceLocation("single_chunk_loader_tile"));
            GameRegistry.registerTileEntity(ChunkLoaderTile.BasicChunkLoaderTile.class, new ResourceLocation("basic_chunk_loader_tile"));
            GameRegistry.registerTileEntity(ChunkLoaderTile.AdvancedChunkLoaderTile.class, new ResourceLocation("advanced_chunk_loader_tile"));
            GameRegistry.registerTileEntity(ChunkLoaderTile.UltimateChunkLoaderTile.class, new ResourceLocation("ultimate_chunk_loader_tile"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            e.getRegistry().register(new ItemBlock(single_chunk_loader).setRegistryName("single_chunk_loader"));
            e.getRegistry().register(new ItemBlock(basic_chunk_loader).setRegistryName("basic_chunk_loader"));
            e.getRegistry().register(new ItemBlock(advanced_chunk_loader).setRegistryName("advanced_chunk_loader"));
            e.getRegistry().register(new ItemBlock(ultimate_chunk_loader).setRegistryName("ultimate_chunk_loader"));
        }
    }

}
