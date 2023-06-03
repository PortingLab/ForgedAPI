package org.portinglab.fabricapi.api;


import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

public interface ExtendedChunkHolder {

    void setCurrentlyLoadingWorldChunk(@Nullable WorldChunk worldChunk);
}
