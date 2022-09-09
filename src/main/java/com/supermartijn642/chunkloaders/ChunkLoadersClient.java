package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.screen.ChunkLoaderScreen;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
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

    public static void register(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get("chunkloaders");
        for(ChunkLoaderType type : ChunkLoaderType.values())
            handler.registerCustomBlockEntityRenderer(type::getBlockEntityType, () -> new ChunkLoaderBlockEntityRenderer(type.getBlock(), type.getFullRotation()));
    }

    public static void setup(){
        // Register key to open chunk loader screen
        CHUNK_LOADING_SCREEN_KEY = new KeyBinding("chunkloaders.keys.open_screen", 46/*'c'*/, "chunkloaders.keys.category");
        ClientRegistry.registerKeyBinding(CHUNK_LOADING_SCREEN_KEY);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent e){
        if(CHUNK_LOADING_SCREEN_KEY != null && CHUNK_LOADING_SCREEN_KEY.isPressed() && ClientUtils.getWorld() != null && ClientUtils.getMinecraft().currentScreen == null){
            EntityPlayer player = ClientUtils.getPlayer();
            if(ChunkLoadersConfig.canPlayersUseMap.get())
                ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(new ChunkPos(player.getPosition()), player.getUniqueID(), player.getPosition().getY(), 15, 11)));
            else
                player.sendStatusMessage(TextComponents.translation("chunkloaders.gui.disabled").color(TextFormatting.RED).get(), true);
        }
    }

    public static void openChunkLoaderScreen(ChunkLoaderBlockEntity entity){
        int size = entity.getChunkLoaderType().getGridSize() + 2;
        ClientUtils.displayScreen(WidgetScreen.of(new ChunkLoaderScreen(new ChunkPos(entity.getPos()), entity.getOwner(), entity.getPos().getY(), size, size)));
    }
}
