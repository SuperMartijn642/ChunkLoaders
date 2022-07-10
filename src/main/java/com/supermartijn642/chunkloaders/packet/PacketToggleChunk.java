package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ServerChunkLoadingCapability;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class PacketToggleChunk implements BasePacket {

    private UUID owner;
    private ChunkPos pos;

    public PacketToggleChunk(UUID owner, ChunkPos pos){
        this.owner = owner;
        this.pos = pos;
    }

    public PacketToggleChunk(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeUUID(this.owner);
        buffer.writeLong(this.pos.toLong());
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.owner = buffer.readUUID();
        this.pos = new ChunkPos(buffer.readLong());
    }

    @Override
    public void handle(PacketContext context){
        Player player = context.getSendingPlayer();
        // Prevent malicious packets from setting other players' loaded chunks
        if(!this.owner.equals(player.getUUID()) && !player.getServer().getPlayerList().isOp(player.getGameProfile()))
            return;

        ServerChunkLoadingCapability capability = ChunkLoadingCapability.get(player.getCommandSenderWorld()).castServer();

        if(capability.isChunkLoadedByPlayer(this.owner, this.pos))
            capability.stopLoadingChunk(this.owner, this.pos);
        else{
            // Check if the player can load the chunk
            if(capability.canPlayerLoadChunk(this.owner, this.pos))
                capability.startLoadingChunk(this.owner, this.pos);
        }
    }
}
