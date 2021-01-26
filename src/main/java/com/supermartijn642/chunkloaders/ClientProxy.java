package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

    private static final Map<Block,BiFunction<World,BlockPos,ChunkLoaderScreen>> screens = new HashMap<>();

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ChunkLoaders.single_chunk_loader), 0, new ModelResourceLocation(ChunkLoaders.single_chunk_loader.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ChunkLoaders.basic_chunk_loader), 0, new ModelResourceLocation(ChunkLoaders.basic_chunk_loader.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ChunkLoaders.advanced_chunk_loader), 0, new ModelResourceLocation(ChunkLoaders.advanced_chunk_loader.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ChunkLoaders.ultimate_chunk_loader), 0, new ModelResourceLocation(ChunkLoaders.ultimate_chunk_loader.getRegistryName(), "inventory"));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e){
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.SingleChunkLoaderTile.class, new ChunkLoaderTileRenderer(() -> ChunkLoaders.single_chunk_loader, false));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.BasicChunkLoaderTile.class, new ChunkLoaderTileRenderer(() -> ChunkLoaders.basic_chunk_loader, false));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.AdvancedChunkLoaderTile.class, new ChunkLoaderTileRenderer(() -> ChunkLoaders.advanced_chunk_loader, true));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.UltimateChunkLoaderTile.class, new ChunkLoaderTileRenderer(() -> ChunkLoaders.ultimate_chunk_loader, true));
    }

    @SubscribeEvent
    public static void registerScreens(RegistryEvent.Register<Item> e){
        screens.put(ChunkLoaders.single_chunk_loader, (world, pos) -> new ChunkLoaderScreen("single_chunk_loader", world, pos));
        screens.put(ChunkLoaders.basic_chunk_loader, (world, pos) -> new ChunkLoaderScreen("basic_chunk_loader", world, pos));
        screens.put(ChunkLoaders.advanced_chunk_loader, (world, pos) -> new ChunkLoaderScreen("advanced_chunk_loader", world, pos));
        screens.put(ChunkLoaders.ultimate_chunk_loader, (world, pos) -> new ChunkLoaderScreen("ultimate_chunk_loader", world, pos));
    }

    public static void openScreen(Block block, World world, BlockPos pos){
        if(screens.containsKey(block))
            Minecraft.getMinecraft().displayGuiScreen(screens.get(block).apply(world, pos));
    }

}
