package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.CoreSide;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;

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
    public void write(PacketBuffer buffer){
        buffer.writeUniqueId(this.player);
        buffer.writeInt(this.pos.x);
        buffer.writeInt(this.pos.z);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.player = buffer.readUniqueId();
        this.pos = new ChunkPos(buffer.readInt(), buffer.readInt());
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
