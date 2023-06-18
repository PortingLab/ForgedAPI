package org.portinglab.fabricapi.api.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeServerChunkEvents {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChunkLoadEvent(ChunkEvent.Load event) {
        if (event.getWorld() instanceof ServerWorld world) {
            WorldChunk chunk = (WorldChunk) event.getChunk();
            ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(world, chunk);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onChunkUnloadEvent(ChunkEvent.Unload event) {
        if (event.getWorld() instanceof ServerWorld world) {
            WorldChunk chunk = (WorldChunk) event.getChunk();
            ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(world, chunk);
        }
    }
}
