package com.supermartijn642.chunkloaders;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

    public static Capability<ChunkTracker> TRACKER_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent e){
        e.register(ChunkTracker.class);
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
                tracker.ifPresent(cap -> cap.read((CompoundTag)nbt));
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

        public CompoundTag write(){
            CompoundTag compound = new CompoundTag();
            for(Map.Entry<ChunkPos,List<BlockPos>> entry : this.chunks.entrySet()){
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putLong("chunk", entry.getKey().toLong());

                LongArrayTag blocks = new LongArrayTag(entry.getValue().stream().map(BlockPos::asLong).collect(Collectors.toList()));
                chunkTag.put("blocks", blocks);

                compound.put(entry.getKey().x + ";" + entry.getKey().z + ";old", chunkTag);
            }
            return compound;
        }

        public void read(CompoundTag compound){
            for(String key : compound.getAllKeys()){
                CompoundTag chunkTag = compound.getCompound(key);
                ChunkPos chunk = new ChunkPos(chunkTag.getLong("chunk"));
                LongArrayTag blocks = (LongArrayTag)chunkTag.get("blocks");
                List<BlockPos> blockList = new ArrayList<>();
                Arrays.stream(blocks.getAsLongArray()).mapToObj(BlockPos::of).forEach(blockList::add);

                this.chunks.put(chunk, blockList);
                if(!key.endsWith(";old"))
                    this.world.getServer().addTickable(() -> this.world.setChunkForced(chunk.x, chunk.z, false));
            }
        }

        public void onLoadLevel(ForgeChunkManager.TicketHelper ticketHelper){
            // Clear all tickets and reload the ones from the capability to naturally fix possible errors when the world is reloaded
            ticketHelper.removeAllTickets(LEGACY_GLOBAL_UUID);
            if(ChunkLoadersConfig.allowLegacyLoadedChunks.get()){
                for(ChunkPos pos : this.chunks.keySet())
                    ForgeChunkManager.forceChunk(this.world, "chunkloaders", LEGACY_GLOBAL_UUID, pos.x, pos.z, true, true);
            }
        }
    }
}
