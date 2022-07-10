package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.ChunkLoaderType;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.CoreSide;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
public class PackedChunkLoaderAdded implements BasePacket {

    private BlockPos pos;
    private UUID owner;
    private ChunkLoaderType type;

    public PackedChunkLoaderAdded(BlockPos pos, UUID owner, ChunkLoaderType type){
        this.pos = pos;
        this.owner = owner;
        this.type = type;
    }

    public PackedChunkLoaderAdded(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeUUID(this.owner);
        buffer.writeEnum(this.type);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
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
        ChunkLoadingCapability.get(context.getWorld()).castClient().addChunkLoader(this.pos, this.owner, this.type);
    }
}
