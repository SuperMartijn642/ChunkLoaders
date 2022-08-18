package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ClientChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.PlayerActivityTracker;
import com.supermartijn642.chunkloaders.capability.ServerChunkLoadingCapability;
import com.supermartijn642.chunkloaders.packet.*;
import com.supermartijn642.core.network.PacketChannel;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created 7/7/2020 by SuperMartijn642
 */
@Mod(modid = ChunkLoaders.MODID, name = ChunkLoaders.NAME, version = ChunkLoaders.VERSION, dependencies = ChunkLoaders.DEPENDENCIES)
public class ChunkLoaders {

    public static final String MODID = "chunkloaders";
    public static final String NAME = "Chunk Loaders";
    public static final String VERSION = "1.2.2";
    public static final String DEPENDENCIES = "required-after:supermartijn642corelib@[1.0.19,);required-after:supermartijn642configlib@[1.1.6,)";

    @CapabilityInject(ChunkLoadingCapability.class)
    public static Capability<ChunkLoadingCapability> CHUNK_LOADING_CAPABILITY;

    public static final Logger LOGGER = LogManager.getLogger("chunkloaders");
    public static final PacketChannel CHANNEL = PacketChannel.create("chunkloaders");

    public static final CreativeTabs GROUP = new CreativeTabs("chunkloaders") {
        @Override
        public ItemStack getTabIconItem(){
            return new ItemStack(ChunkLoaderType.ADVANCED.getItem());
        }
    };

    @Mod.Instance
    public static ChunkLoaders instance;

    public ChunkLoaders(){
        CHANNEL.registerMessage(PackedChunkLoaderAdded.class, PackedChunkLoaderAdded::new, true);
        CHANNEL.registerMessage(PackedChunkLoaderRemoved.class, PackedChunkLoaderRemoved::new, true);
        CHANNEL.registerMessage(PackedStartLoadingChunk.class, PackedStartLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedStopLoadingChunk.class, PackedStopLoadingChunk::new, true);
        CHANNEL.registerMessage(PackedTogglePlayerActivity.class, PackedTogglePlayerActivity::new, true);
        CHANNEL.registerMessage(PacketFullCapabilityData.class, PacketFullCapabilityData::new, true);
        CHANNEL.registerMessage(PacketToggleChunk.class, PacketToggleChunk::new, true);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e){
        // Load client stuff
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
            ChunkLoadersClient.setup();

        // Register the legacy capability
        LegacyChunkLoadingCapability.register();

        // Register the chunk loading capability
        CapabilityManager.INSTANCE.register(ChunkLoadingCapability.class, new Capability.IStorage<ChunkLoadingCapability>() {
            public NBTBase writeNBT(Capability<ChunkLoadingCapability> capability, ChunkLoadingCapability instance, EnumFacing side){
                return instance.write();
            }

            public void readNBT(Capability<ChunkLoadingCapability> capability, ChunkLoadingCapability instance, EnumFacing side, NBTBase nbt){
                instance.read((NBTTagCompound)nbt);
            }
        }, () -> new ClientChunkLoadingCapability(null));

        // Set the chunk loading callback
        ForgeChunkManager.setForcedChunkLoadingCallback(this, (tickets, level) -> {
            level.getCapability(CHUNK_LOADING_CAPABILITY, null).castServer().onLoadLevel(tickets);
            level.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY, null).onLoadLevel(tickets);
        });
    }

    @SubscribeEvent
    public static void onServerStarting(FMLServerAboutToStartEvent e){
        PlayerActivityTracker.onServerStarting(e);
    }

    @Mod.EventBusSubscriber
    public static class ModBusEvents {

        @SubscribeEvent
        public static void attachCapabilities(AttachCapabilitiesEvent<World> e){
            World level = e.getObject();
            ChunkLoadingCapability tracker = level.isRemote ? new ClientChunkLoadingCapability(level) : new ServerChunkLoadingCapability(level);
            e.addCapability(new ResourceLocation("chunkloaders", "chunk_loading_capability"), new ICapabilitySerializable<NBTBase>() {
                @Nonnull
                @Override
                public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side){
                    return cap == CHUNK_LOADING_CAPABILITY ? CHUNK_LOADING_CAPABILITY.cast(tracker) : null;
                }

                @Override
                public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing facing){
                    return cap == CHUNK_LOADING_CAPABILITY;
                }

                @Override
                public NBTBase serializeNBT(){
                    return CHUNK_LOADING_CAPABILITY.writeNBT(tracker, null);
                }

                @Override
                public void deserializeNBT(NBTBase nbt){
                    CHUNK_LOADING_CAPABILITY.readNBT(tracker, null, nbt);
                }
            });
        }

        @SubscribeEvent
        public static void onBlockRegistry(final RegistryEvent.Register<Block> e){
            for(ChunkLoaderType type : ChunkLoaderType.values())
                type.registerBlock(e.getRegistry());
            for(ChunkLoaderType type : ChunkLoaderType.values())
                type.registerTileEntity();
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> e){
            for(ChunkLoaderType type : ChunkLoaderType.values())
                type.registerItem(e.getRegistry());
        }
    }

}
