package com.supermartijn642.chunkloaders;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.LongArrayNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkLoaderUtil {

    @CapabilityInject(ChunkTracker.class)
    public static Capability<ChunkTracker> TRACKER_CAPABILITY;

    public static void register(){
        CapabilityManager.INSTANCE.register(ChunkTracker.class, new Capability.IStorage<ChunkTracker>() {
            public CompoundNBT writeNBT(Capability<ChunkTracker> capability, ChunkTracker instance, Direction side){
                return instance.write();
            }

            public void readNBT(Capability<ChunkTracker> capability, ChunkTracker instance, Direction side, INBT nbt){
                instance.read((CompoundNBT)nbt);
            }
        }, ChunkTracker::new);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
        World world = e.getObject();
        if(world.isRemote || !(world instanceof ServerWorld))
            return;

        LazyOptional<ChunkTracker> tracker = LazyOptional.of(() -> new ChunkTracker((ServerWorld)world));
        e.addCapability(new ResourceLocation("chunkloaders", "chunk_tracker"), new ICapabilitySerializable<INBT>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == TRACKER_CAPABILITY ? tracker.cast() : LazyOptional.empty();
            }

            @Override
            public INBT serializeNBT(){
                return TRACKER_CAPABILITY.writeNBT(tracker.orElse(null), null);
            }

            @Override
            public void deserializeNBT(INBT nbt){
                TRACKER_CAPABILITY.readNBT(tracker.orElse(null), null, nbt);
            }
        });
        e.addListener(tracker::invalidate);
    }

    public static class ChunkTracker {

        private final ServerWorld world;
        private final Map<ChunkPos,List<BlockPos>> chunks = new HashMap<>();

        public ChunkTracker(ServerWorld world){
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
                this.world.forceChunk(chunk.x, chunk.z, true);
            }

            this.chunks.get(chunk).add(loader);
        }

        public void remove(ChunkPos chunk, BlockPos loader){
            if(!this.chunks.containsKey(chunk) || !this.chunks.get(chunk).contains(loader))
                return;

            if(this.chunks.get(chunk).size() == 1){
                this.world.forceChunk(chunk.x, chunk.z, false);
                this.chunks.remove(chunk);
            }else
                this.chunks.get(chunk).remove(loader);
        }

        public CompoundNBT write(){
            CompoundNBT compound = new CompoundNBT();
            for(Map.Entry<ChunkPos,List<BlockPos>> entry : this.chunks.entrySet()){
                CompoundNBT chunkTag = new CompoundNBT();
                chunkTag.putLong("chunk", entry.getKey().asLong());

                LongArrayNBT blocks = new LongArrayNBT(entry.getValue().stream().map(BlockPos::toLong).collect(Collectors.toList()));
                chunkTag.put("blocks", blocks);

                compound.put(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
            }
            return compound;
        }

        public void read(CompoundNBT compound){
            for(String key : compound.keySet()){
                CompoundNBT chunkTag = compound.getCompound(key);
                ChunkPos chunk = new ChunkPos(chunkTag.getLong("chunk"));

                LongArrayNBT blocks = (LongArrayNBT)chunkTag.get("blocks");
                Arrays.stream(blocks.getAsLongArray()).mapToObj(BlockPos::fromLong).forEach(pos -> this.add(chunk, pos));
            }
        }

    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END || !(e.world instanceof ServerWorld))
            return;

        ServerWorld world = (ServerWorld)e.world;
        ServerChunkProvider chunkProvider = world.getChunkProvider();
        int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
        if(tickSpeed > 0){
            world.getCapability(TRACKER_CAPABILITY).ifPresent(tracker -> {
                for(ChunkPos pos : tracker.chunks.keySet()){
                    if(chunkProvider.chunkManager.getTrackingPlayers(pos, false).count() == 0)
                        world.tickEnvironment(world.getChunk(pos.x, pos.z), tickSpeed);
                }
            });
        }
    }

}
