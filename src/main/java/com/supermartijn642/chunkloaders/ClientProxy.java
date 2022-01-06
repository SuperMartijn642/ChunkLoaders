package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientProxy {

    private static final Map<Block,BiFunction<World,BlockPos,ChunkLoaderScreen>> screens = new HashMap<>();

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.SingleChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.single_chunk_loader, false));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.BasicChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.basic_chunk_loader, false));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.AdvancedChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.advanced_chunk_loader, true));
        ClientRegistry.bindTileEntitySpecialRenderer(ChunkLoaderTile.UltimateChunkLoaderTile.class, new ChunkLoaderTileRenderer(ChunkLoaders.ultimate_chunk_loader, true));

        screens.put(ChunkLoaders.single_chunk_loader, (world, pos) -> new ChunkLoaderScreen("single_chunk_loader", world, pos));
        screens.put(ChunkLoaders.basic_chunk_loader, (world, pos) -> new ChunkLoaderScreen("basic_chunk_loader", world, pos));
        screens.put(ChunkLoaders.advanced_chunk_loader, (world, pos) -> new ChunkLoaderScreen("advanced_chunk_loader", world, pos));
        screens.put(ChunkLoaders.ultimate_chunk_loader, (world, pos) -> new ChunkLoaderScreen("ultimate_chunk_loader", world, pos));
    }

    public static void openScreen(Block block, World world, BlockPos pos){
        if(screens.containsKey(block))
            Minecraft.getInstance().setScreen(screens.get(block).apply(world, pos));
    }

}
