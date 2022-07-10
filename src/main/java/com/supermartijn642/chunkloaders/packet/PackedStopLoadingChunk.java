package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.CoreSide;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
public class PackedStopLoadingChunk implements BasePacket {

    private UUID player;
    private ChunkPos pos;

    public PackedStopLoadingChunk(UUID player, ChunkPos pos){
        this.player = player;
        this.pos = pos;
    }

    public PackedStopLoadingChunk(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeUUID(this.player);
        buffer.writeLong(this.pos.toLong());
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.player = buffer.readUUID();
        this.pos = new ChunkPos(buffer.readLong());
    }

    @Override
    public boolean verify(PacketContext context){
        return context.getHandlingSide() == CoreSide.CLIENT;
    }

    @Override
    public void handle(PacketContext context){
        ChunkLoadingCapability.get(context.getWorld()).castClient().stopLoadingChunk(this.player, this.pos);
    }
}
