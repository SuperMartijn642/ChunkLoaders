package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Created 7/11/2020 by SuperMartijn642
 */
public abstract class ChunkLoaderPacket {

    protected BlockPos pos;

    public ChunkLoaderPacket(BlockPos pos){
        this.pos = pos;
    }

    public ChunkLoaderPacket(FriendlyByteBuf buffer){
        this.decodeBuffer(buffer);
    }

    public void encode(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
    }

    protected void decodeBuffer(FriendlyByteBuf buffer){
        this.pos = buffer.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier){
        contextSupplier.get().setPacketHandled(true);

        Player player = contextSupplier.get().getSender();
        if(player == null || player.blockPosition().distSqr(this.pos) > 32 * 32)
            return;
        Level world = player.level;
        if(world == null)
            return;
        BlockEntity tile = world.getBlockEntity(this.pos);
        if(tile instanceof ChunkLoaderTile)
            contextSupplier.get().enqueueWork(() -> this.handle(player, world, (ChunkLoaderTile)tile));
    }

    protected abstract void handle(Player player, Level world, ChunkLoaderTile tile);
}
