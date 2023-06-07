package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketFullCapabilityData;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
public class ChunkLoadingEventHandler {

    public static void registerCallbacks(){
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> onPlayerEnterLevel(player));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player));
    }

    public static void onPlayerEnterLevel(Player player){
        if(!(player instanceof ServerPlayer))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(((ServerPlayer)player).level());
        ChunkLoaders.CHANNEL.sendToPlayer(player, new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }

    public static void onPlayerJoin(Player player){
        if(!(player instanceof ServerPlayer))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(player.level());
        ChunkLoaders.CHANNEL.sendToPlayer(player, new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }
}
