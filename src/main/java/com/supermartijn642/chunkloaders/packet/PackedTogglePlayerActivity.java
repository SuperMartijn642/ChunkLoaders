package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.CoreSide;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.network.PacketBuffer;

import java.util.UUID;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
public class PackedTogglePlayerActivity implements BasePacket {

    private UUID player;
    private boolean active;

    public PackedTogglePlayerActivity(UUID player, boolean active){
        this.player = player;
        this.active = active;
    }

    public PackedTogglePlayerActivity(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeUniqueId(this.player);
        buffer.writeBoolean(this.active);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.player = buffer.readUniqueId();
        this.active = buffer.readBoolean();
    }

    @Override
    public boolean verify(PacketContext context){
        return context.getHandlingSide() == CoreSide.CLIENT;
    }

    @Override
    public void handle(PacketContext context){
        ChunkLoadingCapability.get(context.getWorld()).castClient().togglePlayerActivity(this.player, this.active);
    }
}
