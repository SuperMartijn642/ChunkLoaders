package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public class ChunkLoadersClient implements ClientModInitializer {

    private static KeyMapping CHUNK_LOADING_SCREEN_KEY;

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("chunkloaders");
        for(ChunkLoaderType type : ChunkLoaderType.values())
            handler.registerCustomBlockEntityRenderer(type::getBlockEntityType, () -> new ChunkLoaderBlockEntityRenderer(type.getBlock(), type.getFullRotation()));

        // Register key to open chunk loader screen
        CHUNK_LOADING_SCREEN_KEY = new KeyMapping("chunkloaders.keys.open_screen", 67/*'c'*/, "chunkloaders.keys.category");
        KeyBindingHelper.registerKeyBinding(CHUNK_LOADING_SCREEN_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while(CHUNK_LOADING_SCREEN_KEY.consumeClick())
                onKey();
        });
    }

    public static void onKey(){
        if(ClientUtils.getWorld() != null && ClientUtils.getMinecraft().screen == null){
            Player player = ClientUtils.getPlayer();
            if(ChunkLoadersConfig.canPlayersUseMap.get())
                ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(new ChunkPos(player.blockPosition()), player.getUUID(), player.blockPosition().getY(), 15, 11)));
            else
                player.displayClientMessage(TextComponents.translation("chunkloaders.gui.disabled").color(ChatFormatting.RED).get(), true);
        }
    }

    public static void openChunkLoaderScreen(ChunkLoaderBlockEntity entity){
        int size = entity.getChunkLoaderType().getGridSize() + 2;
        ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(new ChunkPos(entity.getBlockPos()), entity.getOwner(), entity.getBlockPos().getY(), size, size)));
    }
}
