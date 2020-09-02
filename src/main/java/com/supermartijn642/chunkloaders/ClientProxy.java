package com.supermartijn642.chunkloaders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntityRenderer(ChunkLoaders.single_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(o, ChunkLoaders.single_chunk_loader, false));
        ClientRegistry.bindTileEntityRenderer(ChunkLoaders.basic_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(o, ChunkLoaders.basic_chunk_loader, false));
        ClientRegistry.bindTileEntityRenderer(ChunkLoaders.advanced_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(o, ChunkLoaders.advanced_chunk_loader, true));
        ClientRegistry.bindTileEntityRenderer(ChunkLoaders.ultimate_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(o, ChunkLoaders.ultimate_chunk_loader, true));
    }

    public static void openScreen(Screen screen){
        Minecraft.getInstance().displayGuiScreen(screen);
    }

}
