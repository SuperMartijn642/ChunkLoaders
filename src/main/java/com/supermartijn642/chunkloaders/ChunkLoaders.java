package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ClientChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ServerChunkLoadingCapability;
import com.supermartijn642.chunkloaders.data.*;
import com.supermartijn642.chunkloaders.packet.*;
import com.supermartijn642.core.network.PacketChannel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("chunkloaders")
public class ChunkLoaders {

    public static Capability<ChunkLoadingCapability> CHUNK_LOADING_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Logger LOGGER = LogManager.getLogger("chunkloaders");
    public static final PacketChannel CHANNEL = PacketChannel.create("chunkloaders");

    public static final CreativeModeTab GROUP = new CreativeModeTab("chunkloaders") {
        @Override
        public ItemStack makeIcon(){
            return new ItemStack(ChunkLoaderType.ADVANCED.getItem());
        }
    };

    public ChunkLoaders(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, this::attachCapabilities);

        CHANNEL.registerMessage(PackedChunkLoaderAdded.class, PackedChunkLoaderAdded::new, true);
        CHANNEL.registerMessage(PackedChunkLoaderRemoved.class, PackedChunkLoaderRemoved::new, true);
        CHANNEL.registerMessage(PackedStartLoadingChunk.class, PackedStartLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedStopLoadingChunk.class, PackedStopLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedTogglePlayerActivity.class, PackedTogglePlayerActivity::new, true);
        CHANNEL.registerMessage(PacketFullCapabilityData.class, PacketFullCapabilityData::new, true);
        CHANNEL.registerMessage(PacketToggleChunk.class, PacketToggleChunk::new, true);
    }

    public void init(FMLCommonSetupEvent e){
        // Set the chunk loading callback
        ForgeChunkManager.setForcedChunkLoadingCallback("chunkloaders", (level, ticketHelper) -> {
            level.getCapability(CHUNK_LOADING_CAPABILITY).ifPresent(capability -> capability.castServer().onLoadLevel(ticketHelper));
            level.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY).ifPresent(capability -> capability.onLoadLevel(ticketHelper));
        });
    }

    public void registerCapabilities(RegisterCapabilitiesEvent e){
        // Register the chunk loading capability
        e.register(ChunkLoadingCapability.class);

        // Register the legacy capability
        LegacyChunkLoadingCapability.register(e);
    }

    public void attachCapabilities(AttachCapabilitiesEvent<Level> e){
        Level level = e.getObject();
        LazyOptional<ChunkLoadingCapability> tracker = LazyOptional.of(() -> level.isClientSide ? new ClientChunkLoadingCapability(level) : new ServerChunkLoadingCapability(level));
        e.addCapability(new ResourceLocation("chunkloaders", "chunk_loading_capability"), new ICapabilitySerializable<>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == CHUNK_LOADING_CAPABILITY ? tracker.cast() : LazyOptional.empty();
            }

            @Override
            public Tag serializeNBT(){
                return tracker.map(ChunkLoadingCapability::write).orElse(null);
            }

            @Override
            public void deserializeNBT(Tag nbt){
                tracker.ifPresent(cap -> cap.read((CompoundTag)nbt));
            }
        });
        e.addListener(tracker::invalidate);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            for(ChunkLoaderType type : ChunkLoaderType.values())
                type.registerBlock(e.getRegistry());
        }

        @SubscribeEvent
        public static void onTileRegistry(final RegistryEvent.Register<BlockEntityType<?>> e){
            for(ChunkLoaderType type : ChunkLoaderType.values())
                type.registerTileEntity(e.getRegistry());
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            for(ChunkLoaderType type : ChunkLoaderType.values())
                type.registerItem(e.getRegistry());
        }

        @SubscribeEvent
        public static void gatherDataProviders(GatherDataEvent e){
            if(e.includeClient()){
                e.getGenerator().addProvider(new ChunkLoadersBlockStateProvider(e.getGenerator(), e.getExistingFileHelper()));
                e.getGenerator().addProvider(new ChunkLoadersItemModelProvider(e.getGenerator(), e.getExistingFileHelper()));
                e.getGenerator().addProvider(new ChunkLoadersLanguageProvider(e.getGenerator()));
            }

            if(e.includeServer()){
//                e.getGenerator().addProvider(new ChunkLoadersAdvancementProvider(e.getGenerator()));
                e.getGenerator().addProvider(new ChunkLoadersBlockTagsProvider(e.getGenerator(), e.getExistingFileHelper()));
                e.getGenerator().addProvider(new ChunkLoadersLootTableProvider(e.getGenerator()));
                e.getGenerator().addProvider(new ChunkLoadersRecipeProvider(e.getGenerator()));
            }
        }
    }
}
