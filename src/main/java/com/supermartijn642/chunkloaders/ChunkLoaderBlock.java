package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlock extends BaseBlock implements EntityHoldingBlock {

    public static final BlockShape SINGLE_SHAPE = BlockShape.createBlockShape(5, 5, 5, 11, 11, 11);
    public static final BlockShape BASIC_SHAPE = BlockShape.createBlockShape(4, 4, 4, 12, 12, 12);
    public static final BlockShape ADVANCED_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);
    public static final BlockShape ULTIMATE_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);

    private final ChunkLoaderType type;

    public ChunkLoaderBlock(ChunkLoaderType type){
        super(false, BlockProperties.create(Material.IRON, MapColor.GRAY).requiresCorrectTool().destroyTime(1.5f).explosionResistance(6));
        this.type = type;
    }

    @Override
    protected InteractionFeedback interact(IBlockState state, World level, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing hitSide, Vec3d hitLocation){
        TileEntity entity = level.getTileEntity(pos);
        if(entity instanceof ChunkLoaderBlockEntity){
            if(((ChunkLoaderBlockEntity)entity).hasOwner()){
                if(level.isRemote)
                    ChunkLoadersClient.openChunkLoaderScreen((ChunkLoaderBlockEntity)entity);
            }else if(player.isSneaking()){ // Legacy stuff
                if(level.isRemote)
                    player.sendStatusMessage(TextComponents.translation("chunkloaders.legacy_success").color(TextFormatting.WHITE).get(), true);
                else{
                    ((ChunkLoaderBlockEntity)entity).setOwner(player.getUniqueID());
                    LegacyChunkLoadingCapability.ChunkTracker cap = level.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY, null);
                    if(cap != null)
                        cap.remove(pos);
                }
            }else if(level.isRemote)
                player.sendStatusMessage(TextComponents.translation("chunkloaders.legacy_message").color(TextFormatting.RED).get(), true);
        }
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
        return this.type.getShape().simplify();
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return this.type.createBlockEntity();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
        TileEntity entity = worldIn.getTileEntity(pos);
        if(entity instanceof ChunkLoaderBlockEntity && placer instanceof EntityPlayer)
            ((ChunkLoaderBlockEntity)entity).setOwner(placer.getUniqueID());
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity entity = worldIn.getTileEntity(pos);
        if(!worldIn.isRemote && entity instanceof ChunkLoaderBlockEntity){
            if(((ChunkLoaderBlockEntity)entity).hasOwner())
                ChunkLoadingCapability.get(worldIn).castServer().removeChunkLoader((ChunkLoaderBlockEntity)entity);
            else{ // Remove from legacy capability
                LegacyChunkLoadingCapability.ChunkTracker cap = worldIn.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY, null);
                if(cap != null)
                    cap.remove(pos);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockAccess level, Consumer<ITextComponent> info, boolean advanced){
        if(this.type.getGridSize() == 1)
            info.accept(TextComponents.translation("chunkloaders.chunk_loader.info.single").color(TextFormatting.AQUA).get());
        else
            info.accept(TextComponents.translation("chunkloaders.chunk_loader.info.multiple", this.type.getGridSize()).color(TextFormatting.AQUA).get());
    }
}
