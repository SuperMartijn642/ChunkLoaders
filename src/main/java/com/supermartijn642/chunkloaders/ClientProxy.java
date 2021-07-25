package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    private static final Map<Block,Function<BlockPos,ChunkLoaderScreen>> screens = new HashMap<>();

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        BlockEntityRenderers.register(ChunkLoaders.single_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(ChunkLoaders.single_chunk_loader, false));
        BlockEntityRenderers.register(ChunkLoaders.basic_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(ChunkLoaders.basic_chunk_loader, false));
        BlockEntityRenderers.register(ChunkLoaders.advanced_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(ChunkLoaders.advanced_chunk_loader, true));
        BlockEntityRenderers.register(ChunkLoaders.ultimate_chunk_loader_tile, o -> new ChunkLoaderTileRenderer(ChunkLoaders.ultimate_chunk_loader, true));

        screens.put(ChunkLoaders.single_chunk_loader, (pos) -> new ChunkLoaderScreen("single_chunk_loader", pos));
        screens.put(ChunkLoaders.basic_chunk_loader, (pos) -> new ChunkLoaderScreen("basic_chunk_loader", pos));
        screens.put(ChunkLoaders.advanced_chunk_loader, (pos) -> new ChunkLoaderScreen("advanced_chunk_loader", pos));
        screens.put(ChunkLoaders.ultimate_chunk_loader, (pos) -> new ChunkLoaderScreen("ultimate_chunk_loader", pos));
    }

    public static void openScreen(Block block, BlockPos pos){
        if(screens.containsKey(block))
            Minecraft.getInstance().setScreen(screens.get(block).apply(pos));
    }

}
