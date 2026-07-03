package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.extensions.ChunkLoadersKeyMappingCategory;
import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public class ChunkLoadersClient {

    private static KeyMapping CHUNK_LOADING_SCREEN_KEY;

    public static void register(FMLJavaModLoadingContext context){
        RegisterKeyMappingsEvent.BUS.addListener(ChunkLoadersClient::registerKeyBindings);
        InputEvent.Key.BUS.addListener(ChunkLoadersClient::onKey);

        ClientRegistrationHandler handler = ClientRegistrationHandler.get("chunkloaders");
        for(ChunkLoaderType type : ChunkLoaderType.values())
            handler.registerCustomBlockEntityRenderer(type::getBlockEntityType, () -> new ChunkLoaderBlockEntityRenderer(type.getBlock(), type.getFullRotation()));
    }

    public static void registerKeyBindings(RegisterKeyMappingsEvent e){
        // Register key to open chunk loader screen
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("chunkloaders", "keys"));
        //noinspection DataFlowIssue
        ((ChunkLoadersKeyMappingCategory)(Object)category).chunkloadersOverwriteLabel(TextComponents.translation("chunkloaders.keys.category").get());
        CHUNK_LOADING_SCREEN_KEY = new KeyMapping("chunkloaders.keys.open_screen", 67/*'c'*/, category);
        e.register(CHUNK_LOADING_SCREEN_KEY);
    }

    public static void onKey(InputEvent.Key e){
        if(CHUNK_LOADING_SCREEN_KEY != null && CHUNK_LOADING_SCREEN_KEY.consumeClick() && ClientUtils.getWorld() != null && ClientUtils.getMinecraft().screen == null){
            Player player = ClientUtils.getPlayer();
            if(ChunkLoadersConfig.canPlayersUseMap.get())
                ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(ChunkPos.containing(player.blockPosition()), player.getUUID(), player.blockPosition().getY(), 15, 11)));
            else
                player.sendOverlayMessage(TextComponents.translation("chunkloaders.gui.disabled").color(ChatFormatting.RED).get());
        }
    }

    public static void openChunkLoaderScreen(ChunkLoaderBlockEntity entity){
        int size = entity.getChunkLoaderType().getGridSize() + 2;
        ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(ChunkPos.containing(entity.getBlockPos()), entity.getOwner(), entity.getBlockPos().getY(), size, size)));
    }
}
