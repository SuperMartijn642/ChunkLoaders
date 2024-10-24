package com.supermartijn642.chunkloaders;

import com.supermartijn642.chunkloaders.capability.ChunkLoadingCapability;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.block.EntityHoldingBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Consumer;

/**
 * Created 7/10/2020 by SuperMartijn642
 */
public class ChunkLoaderBlock extends BaseBlock implements EntityHoldingBlock, SimpleWaterloggedBlock {

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final BlockShape SINGLE_SHAPE = BlockShape.createBlockShape(5, 5, 5, 11, 11, 11);
    public static final BlockShape BASIC_SHAPE = BlockShape.createBlockShape(4, 4, 4, 12, 12, 12);
    public static final BlockShape ADVANCED_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);
    public static final BlockShape ULTIMATE_SHAPE = BlockShape.createBlockShape(3, 3, 3, 13, 13, 13);

    private final ChunkLoaderType type;

    public ChunkLoaderBlock(ChunkLoaderType type){
        super(false, BlockProperties.create().mapColor(MapColor.COLOR_GRAY).sound(SoundType.METAL).requiresCorrectTool().destroyTime(1.5f).explosionResistance(6));
        this.type = type;

        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        BlockEntity entity = level.getBlockEntity(pos);
        if(entity instanceof ChunkLoaderBlockEntity){
            if(((ChunkLoaderBlockEntity)entity).hasOwner()){
                if(level.isClientSide)
                    ChunkLoadersClient.openChunkLoaderScreen((ChunkLoaderBlockEntity)entity);
            }else if(player.isShiftKeyDown()){ // Legacy stuff
                if(level.isClientSide)
                    player.displayClientMessage(TextComponents.translation("chunkloaders.legacy_success").color(ChatFormatting.WHITE).get(), true);
                else
                    ((ChunkLoaderBlockEntity)entity).setOwner(player.getUUID());
            }else if(level.isClientSide)
                player.displayClientMessage(TextComponents.translation("chunkloaders.legacy_message").color(ChatFormatting.RED).get(), true);
        }
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
        return this.type.getShape().getUnderlying();
    }

    @Override
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return this.type.createBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state){
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
        BlockEntity entity = worldIn.getBlockEntity(pos);
        if(entity instanceof ChunkLoaderBlockEntity && placer instanceof Player)
            ((ChunkLoaderBlockEntity)entity).setOwner(placer.getUUID());
        super.setPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
        BlockEntity entity = worldIn.getBlockEntity(pos);
        if(!worldIn.isClientSide && entity instanceof ChunkLoaderBlockEntity && ((ChunkLoaderBlockEntity)entity).hasOwner())
            ChunkLoadingCapability.get(worldIn).castServer().removeChunkLoader((ChunkLoaderBlockEntity)entity);
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState){
        if(newState.is(this)){
            BlockEntity entity = level.getBlockEntity(pos);
            if(entity instanceof ChunkLoaderBlockEntity)
                entity.onLoad();
        }
    }

    @Override
    protected void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        if(this.type.getGridSize() == 1)
            info.accept(TextComponents.translation("chunkloaders.chunk_loader.info.single").color(ChatFormatting.AQUA).get());
        else
            info.accept(TextComponents.translation("chunkloaders.chunk_loader.info.multiple", this.type.getGridSize()).color(ChatFormatting.AQUA).get());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state){
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random){
        if(state.getValue(WATERLOGGED))
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(BlockStateProperties.WATERLOGGED);
    }
}
