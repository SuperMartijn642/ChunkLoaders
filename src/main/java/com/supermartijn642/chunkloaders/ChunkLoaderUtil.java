package com.supermartijn642.chunkloaders;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
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
        CapabilityManager.INSTANCE.register(ChunkTracker.class);
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Level> e){
        Level world = e.getObject();
        if(world.isClientSide || !(world instanceof ServerLevel))
            return;

        LazyOptional<ChunkTracker> tracker = LazyOptional.of(() -> new ChunkTracker((ServerLevel)world));
        e.addCapability(new ResourceLocation("chunkloaders", "chunk_tracker"), new ICapabilitySerializable<>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == TRACKER_CAPABILITY ? tracker.cast() : LazyOptional.empty();
            }

            @Override
            public Tag serializeNBT(){
                return tracker.map(ChunkTracker::write).orElse(null);
            }

            @Override
            public void deserializeNBT(Tag nbt){
                tracker.ifPresent(chunkTracker -> chunkTracker.read(nbt));
            }
        });
        e.addListener(tracker::invalidate);
    }

    public static class ChunkTracker {

        private final ServerLevel world;
        private final Map<ChunkPos,List<BlockPos>> chunks = new HashMap<>();

        public ChunkTracker(ServerLevel world){
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
                this.world.setChunkForced(chunk.x, chunk.z, true);
            }

            this.chunks.get(chunk).add(loader);
        }

        public void remove(ChunkPos chunk, BlockPos loader){
            if(!this.chunks.containsKey(chunk) || !this.chunks.get(chunk).contains(loader))
                return;

            if(this.chunks.get(chunk).size() == 1){
                this.world.setChunkForced(chunk.x, chunk.z, false);
                this.chunks.remove(chunk);
            }else
                this.chunks.get(chunk).remove(loader);
        }

        public CompoundTag write(){
            CompoundTag compound = new CompoundTag();
            for(Map.Entry<ChunkPos,List<BlockPos>> entry : this.chunks.entrySet()){
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putLong("chunk", entry.getKey().toLong());

                LongArrayTag blocks = new LongArrayTag(entry.getValue().stream().map(BlockPos::asLong).collect(Collectors.toList()));
                chunkTag.put("blocks", blocks);

                compound.put(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
            }
            return compound;
        }

        public void read(Tag tag){
            if(tag instanceof CompoundTag compound){
                for(String key : compound.getAllKeys()){
                    CompoundTag chunkTag = compound.getCompound(key);
                    ChunkPos chunk = new ChunkPos(chunkTag.getLong("chunk"));

                    LongArrayTag blocks = (LongArrayTag)chunkTag.get("blocks");
                    Arrays.stream(blocks.getAsLongArray()).mapToObj(BlockPos::of).forEach(pos -> this.add(chunk, pos));
                }
            }
        }

    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END || !(e.world instanceof ServerLevel))
            return;

        ServerLevel world = (ServerLevel)e.world;
        ServerChunkCache chunkProvider = world.getChunkSource();
        int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
        if(tickSpeed > 0){
            world.getCapability(TRACKER_CAPABILITY).ifPresent(tracker -> {
                for(ChunkPos pos : tracker.chunks.keySet()){
                    if(chunkProvider.chunkMap.getPlayers(pos, false).count() == 0)
                        world.tickChunk(world.getChunk(pos.x, pos.z), tickSpeed);
                }
            });
        }
    }

}
