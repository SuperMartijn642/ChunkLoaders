package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.ToolType;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockShape;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlock extends BaseBlock {

    public static final BlockShape SINGLE_SHAPE = BlockShape.createBlockShape(5, 5, 5, 11, 11, 11);
    public static final BlockShape BASIC_SHAPE = BlockShape.createBlockShape(4, 4, 4, 12, 12, 12);
    public static final BlockShape ADVANCED_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);
    public static final BlockShape ULTIMATE_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);

    private final ChunkLoaderType type;

    public ChunkLoaderBlock(ChunkLoaderType type){
        super(type.getRegistryName(), false, Properties.create(Material.IRON, MapColor.GRAY).hardnessAndResistance(1.5f, 6).harvestLevel(1).harvestTool(ToolType.PICKAXE));
        this.type = type;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand handIn, EnumFacing facing, float hitX, float hitY, float hitZ){
        TileEntity entity = worldIn.getTileEntity(pos);
        if(entity instanceof ChunkLoaderBlockEntity){
            if(((ChunkLoaderBlockEntity)entity).hasOwner()){
                if(worldIn.isRemote)
                    ChunkLoadersClient.openChunkLoaderScreen((ChunkLoaderBlockEntity)entity);
            }else if(player.isSneaking()){ // Legacy stuff
                if(worldIn.isRemote)
                    player.sendStatusMessage(TextComponents.translation("chunkloaders.legacy_success").color(TextFormatting.WHITE).get(), true);
                else{
                    ((ChunkLoaderBlockEntity)entity).setOwner(player.getUniqueID());
                    LegacyChunkLoadingCapability.ChunkTracker cap = worldIn.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY, null);
                    if(cap != null)
                        cap.remove(pos);
                }
            }else if(worldIn.isRemote)
                player.sendStatusMessage(TextComponents.translation("chunkloaders.legacy_message").color(TextFormatting.RED).get(), true);
        }
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos){
        return this.type.getShape().simplify();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
        return this.type.getShape().simplify();
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return this.type.createTileEntity();
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
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced){
        if(this.type.getGridSize() == 1)
            tooltip.add(TextComponents.translation("chunkloaders.chunk_loader.info.single").color(TextFormatting.AQUA).format());
        else
            tooltip.add(TextComponents.translation("chunkloaders.chunk_loader.info.multiple", this.type.getGridSize()).color(TextFormatting.AQUA).format());
    }
}
