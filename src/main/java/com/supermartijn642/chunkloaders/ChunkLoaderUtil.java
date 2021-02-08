package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber
public class ChunkLoaderUtil {

    @CapabilityInject(ChunkTracker.class)
    public static Capability<ChunkTracker> TRACKER_CAPABILITY;

    public static void register(){
        CapabilityManager.INSTANCE.register(ChunkTracker.class, new Capability.IStorage<ChunkTracker>() {
            @Override
            public NBTBase writeNBT(Capability<ChunkTracker> capability, ChunkTracker instance, EnumFacing side){
                return instance.write();
            }

            @Override
            public void readNBT(Capability<ChunkTracker> capability, ChunkTracker instance, EnumFacing side, NBTBase nbt){
                instance.read((NBTTagCompound)nbt);
            }
        }, ChunkTracker::new);
        ForgeChunkManager.setForcedChunkLoadingCallback(ChunkLoaders.instance, (tickets, world) -> {
            if(tickets.size() > 0){
                ChunkTracker tracker = world.getCapability(TRACKER_CAPABILITY, null);
                if(tracker != null){
                    tracker.invalidateTicket();
                    tracker.ticket = tickets.get(0);
                }
            }
        });
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World world = e.getObject();
        if(world.isRemote || !(world instanceof WorldServer))
            return;

        ChunkTracker tracker = new ChunkTracker((WorldServer)world);
        e.addCapability(new ResourceLocation("chunkloaders", "chunk_tracker"), new ICapabilitySerializable<NBTBase>() {
            @Override
            public NBTBase serializeNBT(){
                return TRACKER_CAPABILITY.writeNBT(tracker, null);
            }

            @Override
            public void deserializeNBT(NBTBase nbt){
                TRACKER_CAPABILITY.readNBT(tracker, null, nbt);
            }

            @Override
            public boolean hasCapability(Capability<?> capability, EnumFacing facing){
                return capability == TRACKER_CAPABILITY;
            }

            @Override
            public <T> T getCapability(Capability<T> capability, EnumFacing facing){
                return capability == TRACKER_CAPABILITY ? TRACKER_CAPABILITY.cast(tracker) : null;
            }
        });
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e){
        ChunkTracker tracker = e.getWorld().getCapability(TRACKER_CAPABILITY, null);
        if(tracker != null){
            for(Pair<ChunkPos,BlockPos> pair : tracker.pending)
                tracker.add(pair.getKey(), pair.getValue());
            tracker.pending.clear();
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload e){
        World world = e.getWorld();
        ChunkTracker tracker = world.getCapability(TRACKER_CAPABILITY, null);
        if(tracker != null)
            tracker.invalidateTicket();
    }

    public static class ChunkTracker {

        private final WorldServer world;
        private final Map<ChunkPos,List<BlockPos>> chunks = new HashMap<>();

        private final LinkedList<Pair<ChunkPos,BlockPos>> pending = new LinkedList<>();

        private ForgeChunkManager.Ticket ticket;

        public ChunkTracker(WorldServer world){
            this.world = world;
        }

        public ChunkTracker(){
            this.world = null;
        }

        public void add(ChunkPos chunk, BlockPos loader){
            if(this.chunks.containsKey(chunk) && this.chunks.get(chunk).contains(loader))
                return;

            if(!this.chunks.containsKey(chunk)){
                this.chunks.put(chunk, new LinkedList<>());
                if(this.ticket == null)
                    this.ticket = ForgeChunkManager.requestTicket(ChunkLoaders.instance, this.world, ForgeChunkManager.Type.NORMAL);
                ForgeChunkManager.forceChunk(this.ticket, chunk);
            }

            this.chunks.get(chunk).add(loader);
        }

        public void remove(ChunkPos chunk, BlockPos loader){
            if(!this.chunks.containsKey(chunk) || !this.chunks.get(chunk).contains(loader))
                return;

            if(this.chunks.get(chunk).size() == 1){
                if(this.ticket == null)
                    this.ticket = ForgeChunkManager.requestTicket(ChunkLoaders.instance, this.world, ForgeChunkManager.Type.NORMAL);
                ForgeChunkManager.unforceChunk(this.ticket, chunk);
                this.chunks.remove(chunk);
            }else
                this.chunks.get(chunk).remove(loader);
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

                compound.setTag(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
            }
            return compound;
        }

        public void read(NBTTagCompound compound){
            for(String key : compound.getKeySet()){
                NBTTagCompound chunkTag = compound.getCompoundTag(key);
                ChunkPos chunk = new ChunkPos(chunkTag.getInteger("chunkX"), chunkTag.getInteger("chunkY"));

                NBTTagIntArray blocks = (NBTTagIntArray)chunkTag.getTag("blocks");
                int[] arr = blocks.getIntArray();
                for(int i = 0; i < arr.length; i += 3)
                    this.pending.add(new Pair<>(chunk, new BlockPos(arr[i], arr[i + 1], arr[i + 2])));
            }
        }

        public void invalidateTicket(){
            if(this.ticket != null){
                this.ticket = null;
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
            for(ChunkPos pos : tracker.chunks.keySet()){
                if(!world.getPlayerChunkMap().contains(pos.x, pos.z))
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
