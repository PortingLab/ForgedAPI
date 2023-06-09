package org.portinglab.fabricapi.api;

import net.minecraft.util.math.ChunkPos;

public interface ExtendedServerLightingProvider {
    void forgedapi$updateChunkStatus(ChunkPos pos);
}
