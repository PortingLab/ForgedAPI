package org.portinglab.fabricapi.api.extended;

import net.minecraft.world.chunk.WorldChunk;

import javax.annotation.Nullable;

public interface ExtendedChunkHolder {
    void setCurrentlyLoadingWorldChunk(@Nullable WorldChunk worldChunk);
}
