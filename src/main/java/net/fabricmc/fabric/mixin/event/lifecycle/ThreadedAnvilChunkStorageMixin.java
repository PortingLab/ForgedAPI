/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.chunk.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import org.portinglab.fabricapi.api.ExtendedChunkHolder;
import org.portinglab.fabricapi.api.ExtendedServerLightingProvider;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;

@Mixin(value = ThreadedAnvilChunkStorage.class, priority = 10)
public abstract class ThreadedAnvilChunkStorageMixin {
	@Shadow
	@Final
	private ServerWorld world;

	@Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> chunksToUnload;
	@Shadow protected abstract boolean save(Chunk chunk);
	@Shadow @Final private LongSet loadedChunks;
	@Shadow @Final private ServerLightingProvider lightingProvider;
	@Shadow @Final private WorldGenerationProgressListener worldGenerationProgressListener;
	@Shadow @Final private static Logger LOGGER;
	@Shadow @Final private Queue<Runnable> unloadTaskQueue;
	@Shadow @Final private Long2LongMap chunkToNextSaveTimeMs;
	@Shadow @Final private MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> mainExecutor;

	// Chunk (Un)Load events, An explanation:
	// Must of this code is wrapped inside of futures and consumers, so it's generally a mess.

	/**
	 * Injection is inside of tryUnloadChunk.
	 * We inject just after "setLoadedToWorld" is made false, since here the WorldChunk is guaranteed to be unloaded.
	 */
	/*
	@Inject(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setLoadedToWorld(Z)V", shift = At.Shift.AFTER))
	private void onChunkUnload(ChunkHolder chunkHolder, CompletableFuture<Chunk> chunkFuture, long pos, Chunk chunk, CallbackInfo ci) {
		ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, (WorldChunk) chunk);
	}
	 */

	/**
	 * Injection is inside of convertToFullChunk?
	 *
	 * <p>The following is expected contractually
	 *
	 * <ul><li>the chunk being loaded MUST be a WorldChunk.
	 * <li>everything within the chunk has been loaded into the world. Entities, BlockEntities, etc.</ul>
	 */
	/*
	@Inject(method = "method_17227", at = @At("TAIL"))
	private void onChunkLoad(ChunkHolder chunkHolder, Chunk protoChunk, CallbackInfoReturnable<Chunk> callbackInfoReturnable) {
		// We fire the event at TAIL since the chunk is guaranteed to be a WorldChunk then.
		ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.world, (WorldChunk) callbackInfoReturnable.getReturnValue());
	}
	 */
}
