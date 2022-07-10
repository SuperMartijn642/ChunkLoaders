package com.supermartijn642.chunkloaders.packet;

import com.mojang.authlib.GameProfile;
import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.chunkloaders.capability.ServerChunkLoadingCapability;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.ChunkPos;

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
    public void write(PacketBuffer buffer){
        buffer.writeUniqueId(this.owner);
        buffer.writeInt(this.pos.x);
        buffer.writeInt(this.pos.z);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.owner = buffer.readUniqueId();
        this.pos = new ChunkPos(buffer.readInt(), buffer.readInt());
    }

    @Override
    public void handle(PacketContext context){
        EntityPlayer player = context.getSendingPlayer();
        PlayerList playerList = player.getServer().getPlayerList();
        GameProfile profile = player.getGameProfile();
        if(!this.owner.equals(player.getUniqueID()) && (!playerList.canSendCommands(profile) || (playerList.getOppedPlayers().getEntry(profile) != null && playerList.getOppedPlayers().getPermissionLevel(profile) < player.getServer().getOpPermissionLevel())))
            return;

        ServerChunkLoadingCapability capability = ChunkLoadingCapability.get(player.getEntityWorld()).castServer();

        if(capability.isChunkLoadedByPlayer(this.owner, this.pos))
            capability.stopLoadingChunk(this.owner, this.pos);
        else{
            // Check if the player can load the chunk
            if(capability.canPlayerLoadChunk(this.owner, this.pos))
                capability.startLoadingChunk(this.owner, this.pos);
        }
    }
}
