package org.portinglab.fabricapi.mixin;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.WorldChunk;
import org.portinglab.fabricapi.api.extended.ExtendedChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(ChunkHolder.class)
public class ChunkHolderImpl implements ExtendedChunkHolder {
    @Shadow
    private WorldChunk currentlyLoading;

    @Override
    public void setCurrentlyLoadingWorldChunk(@Nullable WorldChunk worldChunk) {
        this.currentlyLoading = worldChunk;
    }
}
