package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChunkLoadersClient {

    private static KeyBinding CHUNK_LOADING_SCREEN_KEY;

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("chunkloaders");
        for(ChunkLoaderType type : ChunkLoaderType.values())
            handler.registerCustomBlockEntityRenderer(type::getBlockEntityType, () -> new ChunkLoaderBlockEntityRenderer(type.getBlock(), type.getFullRotation()));
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e){
        // Register key to open chunk loader screen
        CHUNK_LOADING_SCREEN_KEY = new KeyBinding("chunkloaders.keys.open_screen", 67/*'c'*/, "chunkloaders.keys.category");
        ClientRegistry.registerKeyBinding(CHUNK_LOADING_SCREEN_KEY);
        MinecraftForge.EVENT_BUS.addListener(ChunkLoadersClient::onKey);
    }

    public static void onKey(InputEvent.KeyInputEvent e){
        if(CHUNK_LOADING_SCREEN_KEY != null && CHUNK_LOADING_SCREEN_KEY.matches(e.getKey(), e.getScanCode()) && ClientUtils.getWorld() != null && ClientUtils.getMinecraft().screen == null){
            PlayerEntity player = ClientUtils.getPlayer();
            if(ChunkLoadersConfig.canPlayersUseMap.get())
                ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(new ChunkPos(player.getCommandSenderBlockPosition()), player.getUUID(), player.getCommandSenderBlockPosition().getY(), 15, 11)));
            else
                player.displayClientMessage(TextComponents.translation("chunkloaders.gui.disabled").color(TextFormatting.RED).get(), true);
        }
    }

    public static void openChunkLoaderScreen(ChunkLoaderBlockEntity entity){
        int size = entity.getChunkLoaderType().getGridSize() + 2;
        ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(new ChunkPos(entity.getBlockPos()), entity.getOwner(), entity.getBlockPos().getY(), size, size)));
    }
}
