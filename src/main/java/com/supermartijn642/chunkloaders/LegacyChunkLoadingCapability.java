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
import net.minecraft.world.dimension.DimensionType;
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
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LegacyChunkLoadingCapability {

    private static final UUID LEGACY_GLOBAL_UUID = UUID.fromString("399cf0ed-1eb4-4e3d-92ca-856f579aac86");

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
        if(world.isClientSide || !(world instanceof ServerWorld))
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

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent e){
        for(DimensionType type : DimensionType.getAllTypes())
            e.getServer().getLevel(type).getCapability(TRACKER_CAPABILITY).ifPresent(ChunkTracker::onLoadLevel);
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

        public void remove(BlockPos loader){
            List<ChunkPos> toBeRemoved = new ArrayList<>();
            for(Map.Entry<ChunkPos,List<BlockPos>> entry : this.chunks.entrySet()){
                if(entry.getValue().remove(loader) && entry.getValue().isEmpty())
                    toBeRemoved.add(entry.getKey());
            }

            for(ChunkPos chunkPos : toBeRemoved){
                this.chunks.remove(chunkPos);
                this.world.setChunkForced(chunkPos.x, chunkPos.z, false);
            }
        }

        public CompoundNBT write(){
            CompoundNBT compound = new CompoundNBT();
            for(Map.Entry<ChunkPos,List<BlockPos>> entry : this.chunks.entrySet()){
                CompoundNBT chunkTag = new CompoundNBT();
                chunkTag.putLong("chunk", entry.getKey().toLong());

                LongArrayNBT blocks = new LongArrayNBT(entry.getValue().stream().map(BlockPos::asLong).collect(Collectors.toList()));
                chunkTag.put("blocks", blocks);

                compound.put(entry.getKey().x + ";" + entry.getKey().z, chunkTag);
            }
            return compound;
        }

        public void read(CompoundNBT compound){
            for(String key : compound.getAllKeys()){
                CompoundNBT chunkTag = compound.getCompound(key);
                ChunkPos chunk = new ChunkPos(chunkTag.getLong("chunk"));
                LongArrayNBT blocks = (LongArrayNBT)chunkTag.get("blocks");
                List<BlockPos> blockList = new ArrayList<>();
                Arrays.stream(blocks.getAsLongArray()).mapToObj(BlockPos::of).forEach(blockList::add);

                this.chunks.put(chunk, blockList);
            }
        }

        public void onLoadLevel(){
            // Clear all tickets and reload the ones from the capability to naturally fix possible errors when the world is reloaded
            if(ChunkLoadersConfig.allowLegacyLoadedChunks.get()){
                for(ChunkPos pos : this.chunks.keySet())
                    this.world.getServer().addTickable(() -> this.world.setChunkForced(pos.x, pos.z, ChunkLoadersConfig.allowLegacyLoadedChunks.get()));
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent e){
        if(e.phase != TickEvent.Phase.END || !(e.world instanceof ServerWorld))
            return;

        ServerWorld world = (ServerWorld)e.world;
        ServerChunkProvider chunkProvider = world.getChunkSource();
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
