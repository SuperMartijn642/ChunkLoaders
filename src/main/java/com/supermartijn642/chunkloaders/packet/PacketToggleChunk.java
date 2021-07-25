package com.supermartijn642.chunkloaders.packet;

import com.supermartijn642.chunkloaders.ChunkLoaderTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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

    public PacketToggleChunk(FriendlyByteBuf buffer){
        super(buffer);
    }

    @Override
    public void encode(FriendlyByteBuf buffer){
        super.encode(buffer);
        buffer.writeInt(this.xOffset);
        buffer.writeInt(this.zOffset);
    }

    @Override
    protected void decodeBuffer(FriendlyByteBuf buffer){
        super.decodeBuffer(buffer);
        this.xOffset = buffer.readInt();
        this.zOffset = buffer.readInt();
    }

    public static PacketToggleChunk decode(FriendlyByteBuf buffer){
        return new PacketToggleChunk(buffer);
    }

    @Override
    protected void handle(Player player, Level world, ChunkLoaderTile tile){
        tile.toggle(this.xOffset, this.zOffset);
    }
}
