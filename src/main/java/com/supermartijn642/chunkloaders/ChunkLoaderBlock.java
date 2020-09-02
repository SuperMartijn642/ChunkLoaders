package com.supermartijn642.chunkloaders;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlock extends Block {

    public static final VoxelShape SINGLE_SHAPE = VoxelShapes.create(5 / 16d, 5 / 16d, 5 / 16d, 11 / 16d, 11 / 16d, 11 / 16d);
    public static final VoxelShape BASIC_SHAPE = VoxelShapes.create(4 / 16d, 4 / 16d, 4 / 16d, 12 / 16d, 12 / 16d, 12 / 16d);
    public static final VoxelShape ADVANCED_SHAPE = VoxelShapes.create(3 / 16d, 3 / 16d, 3 / 16d, 13 / 16d, 13 / 16d, 13 / 16d);
    public static final VoxelShape ULTIMATE_SHAPE = VoxelShapes.create(3 / 16d, 3 / 16d, 3 / 16d, 13 / 16d, 13 / 16d, 13 / 16d);

    private final VoxelShape shape;
    private final Supplier<? extends TileEntity> tileProvider;
    private final BiFunction<World,BlockPos,Screen> screenProvider;

    public ChunkLoaderBlock(String registryName, VoxelShape shape, Supplier<? extends TileEntity> tileProvider, BiFunction<World,BlockPos,Screen> screenProvider){
        super(Properties.create(Material.IRON, MaterialColor.GRAY).hardnessAndResistance(1.5f, 6).harvestLevel(1).harvestTool(ToolType.PICKAXE));
        this.setRegistryName(registryName);
        this.shape = shape;
        this.tileProvider = tileProvider;
        this.screenProvider = screenProvider;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult p_225533_6_){
        if(worldIn.isRemote)
            ClientProxy.openScreen(this.screenProvider.apply(worldIn, pos));
        return ActionResultType.SUCCESS;
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
}
