package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.CoreSide;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
public class PackedChunkLoaderRemoved implements BasePacket {

    private BlockPos pos;
    private UUID owner;
    private ChunkLoaderType type;

    public PackedChunkLoaderRemoved(BlockPos pos, UUID owner, ChunkLoaderType type){
        this.pos = pos;
        this.owner = owner;
        this.type = type;
    }

    public PackedChunkLoaderRemoved(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeUUID(this.owner);
        buffer.writeEnum(this.type);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.pos = buffer.readBlockPos();
        this.owner = buffer.readUUID();
        this.type = buffer.readEnum(ChunkLoaderType.class);
    }

    @Override
    public boolean verify(PacketContext context){
        return context.getHandlingSide() == CoreSide.CLIENT;
    }

    @Override
    public void handle(PacketContext context){
        ChunkLoadingCapability.get(context.getWorld()).castClient().removeChunkLoader(this.pos, this.owner, this.type);
    }
}
