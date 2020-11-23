package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 7/8/2020 by SuperMartijn642
 */
public class PacketToggleChunk extends ChunkLoaderPacket<PacketToggleChunk> {

    private int xOffset, zOffset;

    public PacketToggleChunk(BlockPos pos, int xOffset, int zOffset){
        super(pos);
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    public PacketToggleChunk(){
    }

    @Override
    public void encode(ByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.xOffset);
        buffer.writeInt(this.zOffset);
    }

    @Override
    protected void decodeBuffer(ByteBuf buffer){
        super.decodeBuffer(buffer);
        this.xOffset = buffer.readInt();
        this.zOffset = buffer.readInt();
    }

    @Override
    protected void handle(PacketToggleChunk message, EntityPlayer player, World world, ChunkLoaderTile tile){
        tile.toggle(message.xOffset, message.zOffset);
    }
}
