package com.supermartijn642.chunkloaders;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
@Mod.EventBusSubscriber
public class LegacyChunkLoadingCapability {

    @CapabilityInject(ChunkTracker.class)
    public static Capability<ChunkTracker> TRACKER_CAPABILITY;

    public static void register(){
        CapabilityManager.INSTANCE.register(ChunkTracker.class, new Capability.IStorage<ChunkTracker>() {
            public NBTTagCompound writeNBT(Capability<ChunkTracker> capability, ChunkTracker instance, EnumFacing side){
                return instance.write();
            }

            public void readNBT(Capability<ChunkTracker> capability, ChunkTracker instance, EnumFacing side, NBTBase nbt){
                instance.read((NBTTagCompound)nbt);
            }
        }, ChunkTracker::new);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World world = e.getObject();
        if(world.isRemote || !(world instanceof WorldServer))
            return;

        ChunkTracker tracker = new ChunkTracker((WorldServer)world);
        e.addCapability(new ResourceLocation("chunkloaders", "chunk_tracker"), new ICapabilitySerializable<NBTBase>() {
            @Nonnull
            @Override
            public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side){
                return cap == TRACKER_CAPABILITY ? TRACKER_CAPABILITY.cast(tracker) : null;
            }

            @Override
            public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing facing){
                return cap == TRACKER_CAPABILITY;
            }

            @Override
            public NBTBase serializeNBT(){
                return TRACKER_CAPABILITY.writeNBT(tracker, null);
            }

            @Override
            public void deserializeNBT(NBTBase nbt){
                TRACKER_CAPABILITY.readNBT(tracker, null, nbt);
            }
        });
    }

    public static class ChunkTracker {

        private final WorldServer world;
        private final Map<ChunkPos,List<BlockPos>> chunks = new HashMap<>();
        private ForgeChunkManager.Ticket ticket;

        public ChunkTracker(WorldServer world){
            this.world = world;
        }

        public ChunkTracker(){
            this.world = null;
            this.ticket = null;
        }

        public void remove(BlockPos loader){
            List<ChunkPos> toBeRemoved = new ArrayList<>();
            for(Map.Entry<ChunkPos,List<BlockPos>> entry : this.chunks.entrySet()){
                if(entry.getValue().remove(loader) && entry.getValue().isEmpty())
                    toBeRemoved.add(entry.getKey());
            }

            for(ChunkPos chunkPos : toBeRemoved){
                this.chunks.remove(chunkPos);
                ForgeChunkManager.unforceChunk(this.ticket, chunkPos);
            }
        }

        public NBTTagCompound write(){
            NBTTagCompound compound = new NBTTagCompound();
            for(Map.Entry<ChunkPos,List<BlockPos>> entry : this.chunks.entrySet()){
                NBTTagCompound chunkTag = new NBTTagCompound();
                chunkTag.setInteger("chunkX", entry.getKey().x);
                chunkTag.setInteger("chunkY", entry.getKey().z);


                List<Integer> coords = new ArrayList<>(entry.getValue().size() * 3);
                entry.getValue().forEach(pos -> {
                    coords.add(pos.getX()); coords.add(pos.getY());
                    coords.add(pos.getZ());
                });
                NBTTagIntArray blocks = new NBTTagIntArray(coords);
                chunkTag.setTag("blocks", blocks);

                compound.setTag(entry.getKey().x + ";" + entry.getKey().z + ";old", chunkTag);
            }
            return compound;
        }

        public void read(NBTTagCompound compound){
            for(String key : compound.getKeySet()){
                NBTTagCompound chunkTag = compound.getCompoundTag(key);
                ChunkPos chunk = new ChunkPos(chunkTag.getInteger("chunkX"), chunkTag.getInteger("chunkY"));
                NBTTagIntArray blocks = (NBTTagIntArray)chunkTag.getTag("blocks");
                List<BlockPos> blockList = new ArrayList<>();
                int[] arr = blocks.getIntArray();
                for(int i = 0; i < arr.length; i += 3)
                    blockList.add(new BlockPos(arr[i], arr[i + 1], arr[i + 2]));

                this.chunks.put(chunk, blockList);
            }
        }

        public void onLoadLevel(List<ForgeChunkManager.Ticket> tickets){
            // Clear all tickets and reload the ones from the capability to naturally fix possible errors when the world is reloaded
            tickets.forEach(ForgeChunkManager::releaseTicket);
            this.ticket = ForgeChunkManager.requestTicket(ChunkLoaders.instance, this.world, ForgeChunkManager.Type.NORMAL);
            if(ChunkLoadersConfig.allowLegacyLoadedChunks.get()){
                for(ChunkPos pos : this.chunks.keySet())
                    ForgeChunkManager.forceChunk(this.ticket, pos);
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END || !(e.world instanceof WorldServer))
            return;

        WorldServer world = (WorldServer)e.world;
        int tickSpeed = world.getGameRules().getInt("randomTickSpeed");
        ChunkTracker tracker = world.getCapability(TRACKER_CAPABILITY, null);
        if(tickSpeed > 0 && tracker != null){
            loop:
            for(ChunkPos pos : tracker.chunks.keySet()){
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
