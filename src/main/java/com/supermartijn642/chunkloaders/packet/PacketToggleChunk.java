package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created 7/8/2020 by SuperMartijn642
 */
public class PacketToggleChunk extends ChunkLoaderPacket {

    private int xOffset, zOffset;

    public PacketToggleChunk(BlockPos pos, int xOffset, int zOffset){
        super(pos);
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    public PacketToggleChunk(PacketBuffer buffer){
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer){
        super.encode(buffer);
        buffer.writeInt(this.xOffset);
        buffer.writeInt(this.zOffset);
    }

    @Override
    protected void decodeBuffer(PacketBuffer buffer){
        super.decodeBuffer(buffer);
        this.xOffset = buffer.readInt();
        this.zOffset = buffer.readInt();
    }

    public static PacketToggleChunk decode(PacketBuffer buffer){
        return new PacketToggleChunk(buffer);
    }

    @Override
    protected void handle(PlayerEntity player, World world, ChunkLoaderTile tile){
        tile.toggle(this.xOffset, this.zOffset);
    }
}
