package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketFullCapabilityData;
import net.minecraft.entity.player.ServerPlayerEntity;
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
        if(!(e.getPlayer() instanceof ServerPlayerEntity))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(((ServerPlayerEntity)e.getPlayer()).getLevel());
        ChunkLoaders.CHANNEL.sendToPlayer(e.getPlayer(), new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getPlayer() instanceof ServerPlayerEntity))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(((ServerPlayerEntity)e.getPlayer()).getLevel());
        ChunkLoaders.CHANNEL.sendToPlayer(e.getPlayer(), new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.getPlayer() instanceof ServerPlayerEntity))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(((ServerPlayerEntity)e.getPlayer()).getLevel());
        ChunkLoaders.CHANNEL.sendToPlayer(e.getPlayer(), new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }
}
