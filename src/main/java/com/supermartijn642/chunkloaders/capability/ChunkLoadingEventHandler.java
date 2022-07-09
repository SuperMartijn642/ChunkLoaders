package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketFullCapabilityData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class ChunkLoadingEventHandler {

    /**
     * {@link net.minecraft.world.server.ChunkManager#noPlayersCloseForSpawning(ChunkPos)}
     */
    private static final Method noPlayersCloseForSpawning;
    static {
        noPlayersCloseForSpawning = ObfuscationReflectionHelper.findMethod(ChunkManager.class, "func_219243_d", ChunkPos.class);
        noPlayersCloseForSpawning.setAccessible(true);
    }

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
    public static void onServerStarting(FMLServerStartingEvent e){
        for(DimensionType type : DimensionType.getAllTypes())
            ChunkLoadingCapability.get(e.getServer().getLevel(type)).castServer().onLoadLevel();
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END || !(e.world instanceof ServerWorld))
            return;

        ServerWorld world = (ServerWorld)e.world;
        ServerChunkProvider chunkProvider = world.getChunkSource();
        int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if(tickSpeed > 0){
            for(ChunkPos pos : ChunkLoadingCapability.get(world).castServer().getChunksToBeTicked()){
                if(noPlayersCloseForSpawning(chunkProvider.chunkMap, pos))
                    world.tickChunk(world.getChunk(pos.x, pos.z), tickSpeed);
            }
        }
    }

    private static boolean noPlayersCloseForSpawning(ChunkManager chunkManager, ChunkPos chunk) {
        try{
            return (boolean)noPlayersCloseForSpawning.invoke(chunkManager, chunk);
        }catch(InvocationTargetException | IllegalAccessException e){
            return false;
        }
    }
}
