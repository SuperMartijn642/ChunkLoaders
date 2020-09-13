package com.supermartijn642.chunkloaders;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlock extends Block {

    public static final AxisAlignedBB SINGLE_SHAPE = new AxisAlignedBB(5 / 16d, 5 / 16d, 5 / 16d, 11 / 16d, 11 / 16d, 11 / 16d);
    public static final AxisAlignedBB BASIC_SHAPE = new AxisAlignedBB(4 / 16d, 4 / 16d, 4 / 16d, 12 / 16d, 12 / 16d, 12 / 16d);
    public static final AxisAlignedBB ADVANCED_SHAPE = new AxisAlignedBB(3 / 16d, 3 / 16d, 3 / 16d, 13 / 16d, 13 / 16d, 13 / 16d);
    public static final AxisAlignedBB ULTIMATE_SHAPE = new AxisAlignedBB(3 / 16d, 3 / 16d, 3 / 16d, 13 / 16d, 13 / 16d, 13 / 16d);

    private final AxisAlignedBB shape;
    private final Supplier<? extends TileEntity> tileProvider;
    private final BiFunction<World,BlockPos,GuiScreen> screenProvider;

    public ChunkLoaderBlock(String registryName, AxisAlignedBB shape, Supplier<? extends TileEntity> tileProvider, BiFunction<World,BlockPos,GuiScreen> screenProvider){
        super(Material.IRON, MapColor.GRAY);
        this.setRegistryName(registryName);
        this.setUnlocalizedName(ChunkLoaders.MODID + "." + registryName);
        this.setHardness(1.5f);
        this.setResistance(6);
        this.setHarvestLevel("pickaxe", 1);
        this.setCreativeTab(CreativeTabs.SEARCH);
        this.shape = shape;
        this.tileProvider = tileProvider;
        this.screenProvider = screenProvider;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        if(worldIn.isRemote)
            ClientProxy.openScreen(this.screenProvider.apply(worldIn, pos));
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos){
        return this.shape;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
        return this.shape;
    }

    @Override
    public boolean hasTileEntity(IBlockState state){
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state){
        return this.tileProvider.get();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ChunkLoaderTile)
            ((ChunkLoaderTile)tile).loadAll();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ChunkLoaderTile)
            ((ChunkLoaderTile)tile).unloadAll();
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }
}
