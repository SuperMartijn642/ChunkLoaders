package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlock extends BaseBlock implements EntityHoldingBlock, IWaterLoggable {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final BlockShape SINGLE_SHAPE = BlockShape.createBlockShape(5, 5, 5, 11, 11, 11);
    public static final BlockShape BASIC_SHAPE = BlockShape.createBlockShape(4, 4, 4, 12, 12, 12);
    public static final BlockShape ADVANCED_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);
    public static final BlockShape ULTIMATE_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);

    private final ChunkLoaderType type;

    public ChunkLoaderBlock(ChunkLoaderType type){
        super(false, BlockProperties.create(Material.METAL, MaterialColor.COLOR_GRAY).requiresCorrectTool().destroyTime(1.5f).explosionResistance(6));
        this.type = type;

        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, Direction hitSide, Vec3d hitLocation){
        TileEntity entity = level.getBlockEntity(pos);
        if(entity instanceof ChunkLoaderBlockEntity){
            if(((ChunkLoaderBlockEntity)entity).hasOwner()){
                if(level.isClientSide)
                    ChunkLoadersClient.openChunkLoaderScreen((ChunkLoaderBlockEntity)entity);
            }else if(player.isShiftKeyDown()){ // Legacy stuff
                if(level.isClientSide)
                    player.displayClientMessage(TextComponents.translation("chunkloaders.legacy_success").color(TextFormatting.WHITE).get(), true);
                else{
                    ((ChunkLoaderBlockEntity)entity).setOwner(player.getUUID());
                    level.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY).ifPresent(cap -> cap.remove(pos));
                }
            }else if(level.isClientSide)
                player.displayClientMessage(TextComponents.translation("chunkloaders.legacy_message").color(TextFormatting.RED).get(), true);
        }
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context){
        return this.type.getShape().getUnderlying();
    }

    @Override
    public TileEntity createNewBlockEntity(){
        return this.type.createBlockEntity();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state){
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
        TileEntity entity = worldIn.getBlockEntity(pos);
        if(entity instanceof ChunkLoaderBlockEntity && placer instanceof PlayerEntity)
            ((ChunkLoaderBlockEntity)entity).setOwner(placer.getUUID());
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        TileEntity entity = worldIn.getBlockEntity(pos);
        if(!worldIn.isClientSide && entity instanceof ChunkLoaderBlockEntity){
            if(((ChunkLoaderBlockEntity)entity).hasOwner())
                ChunkLoadingCapability.get(worldIn).castServer().removeChunkLoader((ChunkLoaderBlockEntity)entity);
            else // Remove from legacy capability
                worldIn.getCapability(LegacyChunkLoadingCapability.TRACKER_CAPABILITY).ifPresent(cap -> cap.remove(pos));
        }
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void onPlace(BlockState newState, World level, BlockPos pos, BlockState oldState, boolean unknown){
        if(!level.isClientSide && level.getServer() != null && newState.getBlock() == this){
            TileEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ChunkLoaderBlockEntity){
                level.getServer().submit(() -> {
                    if(!entity.isRemoved())
                        entity.onLoad();
                });
            }
        }
    }

    @Override
    protected void appendItemInformation(ItemStack stack, @Nullable IBlockReader level, Consumer<ITextComponent> info, boolean advanced){
        if(this.type.getGridSize() == 1)
            info.accept(TextComponents.translation("chunkloaders.chunk_loader.info.single").color(TextFormatting.AQUA).get());
        else
            info.accept(TextComponents.translation("chunkloaders.chunk_loader.info.multiple", this.type.getGridSize()).color(TextFormatting.AQUA).get());
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context){
        IFluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public IFluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos){
        if(stateIn.getValue(WATERLOGGED))
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.WATERLOGGED);
    }
}
