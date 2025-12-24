package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ClientChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ServerChunkLoadingCapability;
import com.supermartijn642.chunkloaders.generators.*;
import com.supermartijn642.chunkloaders.packet.*;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
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
    public static final CreativeItemGroup GROUP = CreativeItemGroup.create("chunkloaders", ChunkLoaderType.ADVANCED::getItem);

    public ChunkLoaders(FMLJavaModLoadingContext context){
        AttachCapabilitiesEvent.Levels.BUS.addListener(this::attachCapabilities);
        LevelEvent.Load.BUS.addListener(e -> {
            if(!e.getLevel().isClientSide() && e.getLevel() instanceof Level)
                ChunkLoadingCapability.get((Level)e.getLevel()).castServer().onLoadLevel();
        });

        CHANNEL.registerMessage(PackedChunkLoaderAdded.class, PackedChunkLoaderAdded::new, true);
        CHANNEL.registerMessage(PackedChunkLoaderRemoved.class, PackedChunkLoaderRemoved::new, true);
        CHANNEL.registerMessage(PackedStartLoadingChunk.class, PackedStartLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedStopLoadingChunk.class, PackedStopLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedTogglePlayerActivity.class, PackedTogglePlayerActivity::new, true);
        CHANNEL.registerMessage(PacketFullCapabilityData.class, PacketFullCapabilityData::new, true);
        CHANNEL.registerMessage(PacketToggleChunk.class, PacketToggleChunk::new, true);

        register(context);
        if(CommonUtils.getEnvironmentSide().isClient())
            ChunkLoadersClient.register(context);
        registerGenerators();
    }

    public void attachCapabilities(AttachCapabilitiesEvent.Levels e){
        Level level = e.getObject();
        LazyOptional<ChunkLoadingCapability> tracker = LazyOptional.of(() -> level.isClientSide() ? new ClientChunkLoadingCapability(level) : new ServerChunkLoadingCapability(level));
        e.addCapability(ResourceLocation.fromNamespaceAndPath("chunkloaders", "chunk_loading_capability"), new ICapabilitySerializable<>() {
            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
                return cap == CHUNK_LOADING_CAPABILITY ? tracker.cast() : LazyOptional.empty();
            }

            @Override
            public Tag serializeNBT(HolderLookup.Provider provider){
                return tracker.map(ChunkLoadingCapability::write).orElse(null);
            }

            @Override
            public void deserializeNBT(HolderLookup.Provider provider, Tag nbt){
                tracker.ifPresent(cap -> cap.read((CompoundTag)nbt));
            }
        });
        e.addListener(tracker::invalidate);
    }

    private static void register(FMLJavaModLoadingContext context){
        RegistrationHandler handler = RegistrationHandler.get("chunkloaders");
        for(ChunkLoaderType type : ChunkLoaderType.values()){
            handler.registerBlockCallback(type::registerBlock);
            handler.registerBlockEntityTypeCallback(type::registerBlockEntity);
            handler.registerItemCallback(type::registerItem);
        }
        RegisterEvent.getBus(context.getModBusGroup()).addListener(e -> {
            if(e.getRegistryKey() == Registries.TICKET_TYPE){
                ServerChunkLoadingCapability.CHUNK_LOADING_TICKET_TYPE = Registry.register(
                    BuiltInRegistries.TICKET_TYPE,
                    ResourceLocation.fromNamespaceAndPath("chunkloaders", "loaded"),
                    new TicketType(0, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE)
                );
            }
        });
    }

    private static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get("chunkloaders");
        handler.addGenerator(ChunkLoadersModelGenerator::new);
        handler.addGenerator(ChunkLoadersAtlasSourceGenerator::new);
        handler.addGenerator(ChunkLoadersBlockStateGenerator::new);
        handler.addGenerator(ChunkLoadersItemInfoGenerator::new);
        handler.addGenerator(ChunkLoadersLanguageGenerator::new);
        handler.addGenerator(ChunkLoadersLootTableGenerator::new);
        handler.addGenerator(ChunkLoadersRecipeGenerator::new);
        handler.addGenerator(ChunkLoadersTagGenerator::new);
    }
}
