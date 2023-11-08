package com.supermartijn642.chunkloaders.capability;

import com.supermartijn642.chunkloaders.ChunkLoaders;
import com.supermartijn642.chunkloaders.packet.PacketFullCapabilityData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class ChunkLoadingEventHandler {

    @SubscribeEvent
    public static void onPlayerEnterLevel(PlayerEvent.PlayerChangedDimensionEvent e){
        if(!(e.player instanceof EntityPlayerMP))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(e.player.getEntityWorld());
        ChunkLoaders.CHANNEL.sendToPlayer(e.player, new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.player instanceof EntityPlayerMP))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(e.player.getEntityWorld());
        ChunkLoaders.CHANNEL.sendToPlayer(e.player, new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent e){
        if(!(e.player instanceof EntityPlayerMP))
            return;

        ChunkLoadingCapability capability = ChunkLoadingCapability.get(e.player.getEntityWorld());
        ChunkLoaders.CHANNEL.sendToPlayer(e.player, new PacketFullCapabilityData(capability.castServer().writeClientInfo()));
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END || !(e.world instanceof WorldServer))
            return;

        WorldServer world = (WorldServer)e.world;
        int tickSpeed = world.getGameRules().getInt("randomTickSpeed");
        if(tickSpeed > 0){
            loop:
            for(ChunkPos pos : ChunkLoadingCapability.get(world).castServer().getChunksToBeTicked()){
                for(int i = 0; i < world.playerEntities.size(); ++i){
                    EntityPlayerMP player = (EntityPlayerMP)world.playerEntities.get(i);
                    if(player != null && !player.isSpectator() && pos.getDistanceSq(player) < 128 * 128d)
                        continue loop;
                }
                tickEnvironment(world, pos, tickSpeed);
            }
        }
    }

    private static void tickEnvironment(WorldServer world, ChunkPos pos, int tickSpeed){
        Chunk chunk = world.getChunkFromChunkCoords(pos.x, pos.z);
        int j = chunk.x * 16;
        int k = chunk.z * 16;

        for(ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray()){
            if(extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.needsRandomTick()){
                for(int i1 = 0; i1 < tickSpeed; ++i1){
                    int x = world.rand.nextInt(16);
                    int y = world.rand.nextInt(16);
                    int z = world.rand.nextInt(16);
                    IBlockState iblockstate = extendedblockstorage.get(x, y, z);
                    Block block = iblockstate.getBlock();

                    if(block.getTickRandomly()){
                        block.randomTick(world, new BlockPos(j + x, extendedblockstorage.getYLocation() + y, k + z), iblockstate, world.rand);
                    }
                }
            }
        }
    }
}
