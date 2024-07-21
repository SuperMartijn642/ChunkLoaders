package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.generators.*;
import com.supermartijn642.chunkloaders.packet.*;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod("chunkloaders")
public class ChunkLoaders {

    public static final Logger LOGGER = LogManager.getLogger("chunkloaders");
    public static final PacketChannel CHANNEL = PacketChannel.create("chunkloaders");
    public static final CreativeItemGroup GROUP = CreativeItemGroup.create("chunkloaders", ChunkLoaderType.ADVANCED::getItem);

    public static final TicketController TICKET_CONTROLLER = new TicketController(
        ResourceLocation.fromNamespaceAndPath("chunkloaders", "chunks"),
        (level, ticketHelper) -> ChunkLoadingCapability.get(level).castServer().onLoadLevel(ticketHelper)
    );

    public ChunkLoaders(IEventBus eventBus){
        eventBus.addListener(this::registerTicketController);

        CHANNEL.registerMessage(PackedChunkLoaderAdded.class, PackedChunkLoaderAdded::new, true);
        CHANNEL.registerMessage(PackedChunkLoaderRemoved.class, PackedChunkLoaderRemoved::new, true);
        CHANNEL.registerMessage(PackedStartLoadingChunk.class, PackedStartLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedStopLoadingChunk.class, PackedStopLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedTogglePlayerActivity.class, PackedTogglePlayerActivity::new, true);
        CHANNEL.registerMessage(PacketFullCapabilityData.class, PacketFullCapabilityData::new, true);
        CHANNEL.registerMessage(PacketToggleChunk.class, PacketToggleChunk::new, true);

        register();
        if(CommonUtils.getEnvironmentSide().isClient())
            ChunkLoadersClient.register();
        registerGenerators();
    }

    public void registerTicketController(RegisterTicketControllersEvent e){
        e.register(TICKET_CONTROLLER);
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get("chunkloaders");
        for(ChunkLoaderType type : ChunkLoaderType.values()){
            handler.registerBlockCallback(type::registerBlock);
            handler.registerBlockEntityTypeCallback(type::registerBlockEntity);
            handler.registerItemCallback(type::registerItem);
        }
    }

    private static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get("chunkloaders");
        handler.addGenerator(ChunkLoadersModelGenerator::new);
        handler.addGenerator(ChunkLoadersBlockStateGenerator::new);
        handler.addGenerator(ChunkLoadersLanguageGenerator::new);
        handler.addGenerator(ChunkLoadersLootTableGenerator::new);
        handler.addGenerator(ChunkLoadersRecipeGenerator::new);
        handler.addGenerator(ChunkLoadersTagGenerator::new);
    }
}
