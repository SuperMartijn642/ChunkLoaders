package com.supermartijn642.chunkloaders;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlock extends Block implements IWaterLoggable {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape SINGLE_SHAPE = VoxelShapes.create(5 / 16d, 5 / 16d, 5 / 16d, 11 / 16d, 11 / 16d, 11 / 16d);
    public static final VoxelShape BASIC_SHAPE = VoxelShapes.create(4 / 16d, 4 / 16d, 4 / 16d, 12 / 16d, 12 / 16d, 12 / 16d);
    public static final VoxelShape ADVANCED_SHAPE = VoxelShapes.create(3 / 16d, 3 / 16d, 3 / 16d, 13 / 16d, 13 / 16d, 13 / 16d);
    public static final VoxelShape ULTIMATE_SHAPE = VoxelShapes.create(3 / 16d, 3 / 16d, 3 / 16d, 13 / 16d, 13 / 16d, 13 / 16d);

    private final VoxelShape shape;
    private final Supplier<? extends TileEntity> tileProvider;
    private final int gridSize;

    public ChunkLoaderBlock(String registryName, VoxelShape shape, Supplier<? extends TileEntity> tileProvider, int gridSize){
        super(Properties.create(Material.IRON, MaterialColor.GRAY).hardnessAndResistance(1.5f, 6).harvestLevel(1).harvestTool(ToolType.PICKAXE));
        this.setRegistryName(registryName);
        this.shape = shape;
        this.tileProvider = tileProvider;
        this.gridSize = gridSize;

        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, false));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_){
        if(worldIn.isRemote)
            ClientProxy.openScreen(this, worldIn, pos);
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
        return this.shape;
    }

    @Override
    public boolean hasTileEntity(BlockState state){
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world){
        return this.tileProvider.get();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ChunkLoaderTile)
            ((ChunkLoaderTile)tile).loadAll();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof ChunkLoaderTile)
            ((ChunkLoaderTile)tile).unloadAll();
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
        if(this.gridSize == 1)
            tooltip.add(new TranslationTextComponent("chunkloaders.chunk_loader.info.single").applyTextStyle(TextFormatting.AQUA));
        else
            tooltip.add(new TranslationTextComponent("chunkloaders.chunk_loader.info.multiple", this.gridSize).applyTextStyle(TextFormatting.AQUA));
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        IFluidState fluidstate = context.getWorld().getFluidState(context.getPos());
        return this.getDefaultState().with(WATERLOGGED, fluidstate.getFluid() == Fluids.WATER);
    }

    @Override
    public IFluidState getFluidState(BlockState state){
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos){
        if(stateIn.get(WATERLOGGED))
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.WATERLOGGED);
    }
}
