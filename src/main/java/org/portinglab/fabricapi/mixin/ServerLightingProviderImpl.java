package org.portinglab.fabricapi.mixin;

import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.util.math.ChunkPos;
import org.portinglab.fabricapi.api.extended.ExtendedServerLightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLightingProvider.class)
public abstract class ServerLightingProviderImpl implements ExtendedServerLightingProvider {
    @Shadow
    protected abstract void updateChunkStatus(ChunkPos pos);

    public void forgedapi$updateChunkStatus(ChunkPos pos) {
        this.updateChunkStatus(pos);
    }
}
