package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import com.supermartijn642.core.ClientUtils;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class ChunkLoadersClient {

    private static KeyBinding CHUNK_LOADING_SCREEN_KEY;

    public static void setup(){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            ClientRegistry.bindTileEntitySpecialRenderer(type.getBlockEntityClass(), new ChunkLoaderBlockEntityRenderer(type::getBlock, type.getFullRotation()));

        // Register key to open chunk loader screen
        CHUNK_LOADING_SCREEN_KEY = new KeyBinding("chunkloaders.keys.open_screen", 46/*'c'*/, "chunkloaders.keys.category");
        ClientRegistry.registerKeyBinding(CHUNK_LOADING_SCREEN_KEY);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e){
        for(ChunkLoaderType type : ChunkLoaderType.values())
            ModelLoader.setCustomModelResourceLocation(type.getItem(), 0, new ModelResourceLocation(new ResourceLocation("chunkloaders", type.getRegistryName()), "inventory"));
    }

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent e){
        if(CHUNK_LOADING_SCREEN_KEY != null && CHUNK_LOADING_SCREEN_KEY.isPressed() && ClientUtils.getWorld() != null && ClientUtils.getMinecraft().currentScreen == null){
            EntityPlayer player = ClientUtils.getPlayer();
            ClientUtils.displayScreen(new ChunkLoaderScreen(new ChunkPos(player.getPosition()), player.getUniqueID(), player.getPosition().getY(), 15, 11));
        }
    }

    public static void openChunkLoaderScreen(ChunkLoaderBlockEntity entity){
        int size = entity.getChunkLoaderType().getGridSize() + 2;
        ClientUtils.displayScreen(new ChunkLoaderScreen(new ChunkPos(entity.getPos()), entity.getOwner(), entity.getPos().getY(), size, size));
    }
}
