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
public class PackedStartLoadingChunk implements BasePacket {

    private UUID player;
    private ChunkPos pos;
    private boolean active;

    public PackedStartLoadingChunk(UUID player, ChunkPos pos, boolean active){
        this.player = player;
        this.pos = pos;
        this.active = active;
    }

    public PackedStartLoadingChunk(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeUUID(this.player);
        buffer.writeLong(this.pos.toLong());
        buffer.writeBoolean(this.active);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.player = buffer.readUUID();
        this.pos = new ChunkPos(buffer.readLong());
        this.active = buffer.readBoolean();
    }

    @Override
    public boolean verify(PacketContext context){
        return context.getHandlingSide() == CoreSide.CLIENT;
    }

    @Override
    public void handle(PacketContext context){
        ChunkLoadingCapability.get(context.getWorld()).castClient().startLoadingChunk(this.player, this.pos, this.active);
    }
}
