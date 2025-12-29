package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingEventHandler;
import com.supermartijn642.chunkloaders.capability.PlayerActivityTracker;
import com.supermartijn642.chunkloaders.capability.ServerChunkLoadingCapability;
import com.supermartijn642.chunkloaders.generators.*;
import com.supermartijn642.chunkloaders.packet.*;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.TicketType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
public class ChunkLoaders implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("chunkloaders");
    public static final PacketChannel CHANNEL = PacketChannel.create("chunkloaders");
    public static final CreativeItemGroup GROUP = CreativeItemGroup.create("chunkloaders", ChunkLoaderType.ADVANCED::getItem);

    @Override
    public void onInitialize(){
        CHANNEL.registerMessage(PackedChunkLoaderAdded.class, PackedChunkLoaderAdded::new, true);
        CHANNEL.registerMessage(PackedChunkLoaderRemoved.class, PackedChunkLoaderRemoved::new, true);
        CHANNEL.registerMessage(PackedStartLoadingChunk.class, PackedStartLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedStopLoadingChunk.class, PackedStopLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedTogglePlayerActivity.class, PackedTogglePlayerActivity::new, true);
        CHANNEL.registerMessage(PacketFullCapabilityData.class, PacketFullCapabilityData::new, true);
        CHANNEL.registerMessage(PacketToggleChunk.class, PacketToggleChunk::new, true);

        ChunkLoadingEventHandler.registerCallbacks();
        PlayerActivityTracker.registerCallbacks();
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((entity, world) -> {
            if(entity instanceof ChunkLoaderBlockEntity)
                ((ChunkLoaderBlockEntity)entity).onLoad();
        });

        register();
        registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("chunkloaders");
        for(ChunkLoaderType type : ChunkLoaderType.values()){
            handler.registerBlockCallback(type::registerBlock);
            handler.registerBlockEntityTypeCallback(type::registerBlockEntity);
            handler.registerItemCallback(type::registerItem);
        }
        ServerChunkLoadingCapability.CHUNK_LOADING_TICKET_TYPE = Registry.register(
            BuiltInRegistries.TICKET_TYPE,
            Identifier.fromNamespaceAndPath("chunkloaders", "loaded"),
            new TicketType(0, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE)
        );
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
