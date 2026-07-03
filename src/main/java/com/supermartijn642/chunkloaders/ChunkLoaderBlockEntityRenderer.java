package com.supermartijn642.chunkloaders;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.render.CustomBlockEntityRenderer;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import java.util.List;

/**
 * Created 8/18/2020 by SuperMartijn642
 */
public class ChunkLoaderBlockEntityRenderer implements CustomBlockEntityRenderer<ChunkLoaderBlockEntity,ChunkLoaderBlockEntityRenderer.State> {

    private static final Matrix4fc IDENTITY_MATRIX = new Matrix4f().identity();
    private static final RandomSource RANDOM_SOURCE = RandomSource.create();

    private final Block block;
    private final boolean fullRotation;

    public ChunkLoaderBlockEntityRenderer(Block block, boolean fullRotation){
        this.block = block;
        this.fullRotation = fullRotation;
    }

    @Override
    public State createStateHolder(){
        return new State();
    }

    @Override
    public void updateState(State state, ChunkLoaderBlockEntity entity, UpdateContext context){
        state.animationOffset = entity.animationOffset;
        if(!state.hasBlockRenderState){
            BlockState blockState = this.block.defaultBlockState();
            BlockStateModel model = ClientUtils.getMinecraft().getModelManager().getBlockStateModelSet().get(blockState);
            List<BlockStateModelPart> parts = state.blockRenderState.setupModel(IDENTITY_MATRIX, model.hasMaterialFlag(BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState, BakedQuad.FLAG_TRANSLUCENT));
            RANDOM_SOURCE.setSeed(blockState.getSeed(BlockPos.ZERO));
            model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, blockState, RANDOM_SOURCE, parts);
            IntList tintLayers = state.blockRenderState.tintLayers();
            for(BlockTintSource tintSource : ClientUtils.getMinecraft().getBlockColors().getTintSources(blockState))
                tintLayers.add(tintSource.colorInWorld(blockState, BlockAndTintGetter.EMPTY, BlockPos.ZERO));
            state.hasBlockRenderState = true;
        }
    }

    @Override
    public void submit(SubmitNodeCollector output, State state, RenderContext context){
        if(!state.hasBlockRenderState)
            return;

        PoseStack poseStack = context.poseStack();
        poseStack.pushPose();

        int animationOffset = state.animationOffset;
        double offset = Math.sin((System.currentTimeMillis() + animationOffset) % 5000 / 5000f * 2 * Math.PI) * 0.1;
        poseStack.translate(0, offset, 0);

        poseStack.translate(0.5, 0.5, 0.5);
        if(this.fullRotation){
            float angleX = (System.currentTimeMillis() + animationOffset) % 13000 / 13000f * 2 * (float)Math.PI;
            float angleY = (System.currentTimeMillis() + animationOffset) % 15000 / 15000f * 2 * (float)Math.PI;
            float angleZ = (System.currentTimeMillis() + animationOffset) % 16000 / 16000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleX, 1, 0, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleY, 0, 1, 0));
            poseStack.mulPose(new Quaternionf().setAngleAxis(angleZ, 0, 0, 1));
        }else{
            float angle = (System.currentTimeMillis() + animationOffset) % 11000 / 11000f * 2 * (float)Math.PI;
            poseStack.mulPose(new Quaternionf().setAngleAxis(angle, 0, 1, 0));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        ModelFeatureRenderer.CrumblingOverlay breakingOverlay = context.breakingOverlay();
        state.blockRenderState.submit(poseStack, output, context.packedLight(), breakingOverlay == null ? OverlayTexture.NO_OVERLAY : breakingOverlay.progress(), 0);

        poseStack.popPose();
    }

    public static class State {
        private final BlockModelRenderState blockRenderState = new BlockModelRenderState();
        private boolean hasBlockRenderState = false;
        private int animationOffset;
    }
}
