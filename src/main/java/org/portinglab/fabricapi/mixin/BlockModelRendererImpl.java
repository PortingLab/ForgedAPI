package org.portinglab.fabricapi.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.portinglab.fabricapi.api.ExtendedBlockModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.BitSet;

@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererImpl implements ExtendedBlockModelRenderer {
    @Shadow protected abstract void getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, float @Nullable [] box, BitSet flags);

    @Override
    public void froge$getQuadDimensions(BlockRenderView world, BlockState state, BlockPos pos, int[] vertexData, Direction face, float @Nullable [] box, BitSet flags) {
        this.getQuadDimensions(world, state, pos, vertexData, face, box,flags);
    }
}
