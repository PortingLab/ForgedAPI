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
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.*;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.chunk.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import org.portinglab.fabricapi.api.ExtendedChunkHolder;
import org.portinglab.fabricapi.api.ExtendedServerLightingProvider;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.server.world.ThreadedAnvilChunkStorage.addEntitiesFromNbt;

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
	 *
	 * @author Kasualix, TexTrue
	 * @reason insert event invoker
	 */
	/*
	@Overwrite
	private void tryUnloadChunk(long pos, ChunkHolder holder) {
		CompletableFuture<Chunk> completablefuture = holder.getSavingFuture();
		Consumer var10001 = (chunk) -> {
			CompletableFuture<Chunk> completablefuture1 = holder.getSavingFuture();
			if (completablefuture1 != completablefuture) {
				this.tryUnloadChunk(pos, holder);
			} else if (this.chunksToUnload.remove(pos, holder) && chunk != null) {
				if (chunk instanceof WorldChunk) {
					((WorldChunk)chunk).setLoadedToWorld(false);
					ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, (WorldChunk) chunk);
					MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload((Chunk) chunk));
				}

				this.save((Chunk) chunk);
				if (this.loadedChunks.remove(pos) && chunk instanceof WorldChunk) {
					WorldChunk levelchunk = (WorldChunk)chunk;
					this.world.unloadEntities(levelchunk);
				}

				((ExtendedServerLightingProvider) this.lightingProvider).forgedapi$updateChunkStatus(((Chunk) chunk).getPos());
				this.lightingProvider.tick();
				this.worldGenerationProgressListener.setChunkStatus(((Chunk) chunk).getPos(), (ChunkStatus)null);
				this.chunkToNextSaveTimeMs.remove(((Chunk) chunk).getPos().toLong());
			}

		};
		Queue var10002 = this.unloadTaskQueue;
		Objects.requireNonNull(var10002);
		completablefuture.thenAcceptAsync(var10001, var10002::add).whenComplete((void_, throwable) -> {
			if (throwable != null) {
				LOGGER.error("Failed to save chunk {}", holder.getPos(), throwable);
			}

		});
	}
	 */

	@Inject(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z", shift = At.Shift.AFTER))
	private void onChunkUnload(ChunkHolder chunkHolder, CompletableFuture<Chunk> chunkFuture, long pos, Chunk chunk, CallbackInfo ci) {
		ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, (WorldChunk) chunk);
	}

	/**
	 * Injection is inside of convertToFullChunk?
	 *
	 * <p>The following is expected contractually
	 *
	 * <ul><li>the chunk being loaded MUST be a WorldChunk.
	 * <li>everything within the chunk has been loaded into the world. Entities, BlockEntities, etc.</ul>
	 *
	 * @author Kasualix, TexTrue
	 * @reason insert event invoker
	 */
	@Overwrite
	private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> convertToFullChunk(ChunkHolder chunkHolder) {
		CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completablefuture = chunkHolder.getFutureFor(ChunkStatus.FULL.getPrevious());
		return completablefuture.thenApplyAsync((either) -> {
			ChunkStatus chunkstatus = ChunkHolder.getTargetStatusForLevel(chunkHolder.getLevel());
			return !chunkstatus.isAtLeast(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : either.mapLeft((protoChunk) -> {
				ChunkPos chunkpos = chunkHolder.getPos();
				ProtoChunk protochunk = (ProtoChunk)protoChunk;
				WorldChunk levelchunk;
				if (protochunk instanceof ReadOnlyChunk) {
					levelchunk = ((ReadOnlyChunk)protochunk).getWrappedChunk();
				} else {
					levelchunk = new WorldChunk(this.world, protochunk, (chunk) -> {
						addEntitiesFromNbt(this.world, protochunk.getEntities());
					});
					chunkHolder.setCompletedChunk(new ReadOnlyChunk(levelchunk, false));
				}

				levelchunk.setLevelTypeProvider(() -> {
					return ChunkHolder.getLevelType(chunkHolder.getLevel());
				});
				levelchunk.loadEntities();
				if (this.loadedChunks.add(chunkpos.toLong())) {
					levelchunk.setLoadedToWorld(true);

					try {
						((ExtendedChunkHolder) chunkHolder).setCurrentlyLoadingWorldChunk(levelchunk);
						levelchunk.updateAllBlockEntities();
						levelchunk.addChunkTickSchedulers(this.world);
						ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.world, levelchunk);
						MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(levelchunk));
					} finally {
						((ExtendedChunkHolder) chunkHolder).setCurrentlyLoadingWorldChunk(null);
					}
				}

				return levelchunk;
			});
		}, (task) -> {
			MessageListener var10000 = this.mainExecutor;
			long var10002 = chunkHolder.getPos().toLong();
			Objects.requireNonNull(chunkHolder);
			var10000.send(ChunkTaskPrioritySystem.createMessage(task, var10002, chunkHolder::getLevel));
		});
	}

	/*
	@Inject(method = "method_17227", at = @At(value = "TAIL"))
	private void onChunkLoad(ChunkHolder chunkHolder, Chunk protoChunk, CallbackInfoReturnable<Chunk> callbackInfoReturnable) {
		// We fire the event at TAIL since the chunk is guaranteed to be a WorldChunk then.
		ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.world, (WorldChunk) callbackInfoReturnable.getReturnValue());
	}
	 */

}
