package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.CoreSide;
import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

/**
 * Created 26/06/2022 by SuperMartijn642
 */
public class PacketFullCapabilityData implements BasePacket {

    private CompoundNBT data;

    public PacketFullCapabilityData(CompoundNBT data){
        this.data = data;
    }

    public PacketFullCapabilityData(){
    }

    @Override
    public void write(PacketBuffer buffer){
        buffer.writeNbt(this.data);
    }

    @Override
    public void read(PacketBuffer buffer){
        this.data = buffer.readNbt();
    }

    @Override
    public boolean verify(PacketContext context){
        return context.getHandlingSide() == CoreSide.CLIENT;
    }

    @Override
    public void handle(PacketContext context){
        ChunkLoadingCapability.get(context.getWorld()).castClient().readServerInfo(this.data);
    }
}
