package com.supermartijn642.chunkloaders;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy {

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

    public static void openScreen(GuiScreen screen){
        Minecraft.getMinecraft().displayGuiScreen(screen);
    }

}
