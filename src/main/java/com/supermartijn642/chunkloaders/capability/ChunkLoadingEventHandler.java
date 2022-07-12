package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketFullCapabilityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class ChunkLoadingEventHandler {

    @SubscribeEvent
    public static void onPlayerEnterLevel(PlayerEvent.PlayerChangedDimensionEvent e){
        if(!(e.getEntity() instanceof ServerPlayer))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(((ServerPlayer)e.getEntity()).getLevel());
        ChunkLoaders.CHANNEL.sendToPlayer(e.getEntity(), new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(((ServerPlayer)e.getEntity()).getLevel());
        ChunkLoaders.CHANNEL.sendToPlayer(e.getEntity(), new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }
}
