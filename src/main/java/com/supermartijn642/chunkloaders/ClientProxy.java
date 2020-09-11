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
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.SingleChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.single_chunk_loader, false));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.BasicChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.basic_chunk_loader, false));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.AdvancedChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.advanced_chunk_loader, true));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.UltimateChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.ultimate_chunk_loader, true));
    }

    public static void openScreen(Screen screen){
        Minecraft.getInstance().displayGuiScreen(screen);
    }

}
