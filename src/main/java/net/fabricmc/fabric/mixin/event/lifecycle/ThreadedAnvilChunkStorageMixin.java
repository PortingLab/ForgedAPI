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

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.*;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.chunk.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import org.apache.logging.log4j.Logger;
import org.portinglab.fabricapi.api.ExtendedChunkHolder;
import org.portinglab.fabricapi.api.ExtendedServerLightingProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(value = ThreadedAnvilChunkStorage.class, priority = 10)
public abstract class ThreadedAnvilChunkStorageMixin {
	@Shadow @Final private ServerWorld world;
	@Shadow @Final private Long2ObjectLinkedOpenHashMap<ChunkHolder> chunksToUnload;
	@Shadow protected abstract boolean save(Chunk chunk);
	@Shadow @Final private LongSet loadedChunks;
	@Shadow @Final private ServerLightingProvider serverLightingProvider;
	@Shadow @Final private WorldGenerationProgressListener worldGenerationProgressListener;
	@Shadow @Final private static Logger LOGGER;
	@Shadow @Final private Queue<Runnable> unloadTaskQueue;

	@Shadow @Final private MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> mainExecutor;

	/*
	  Injection is inside of tryUnloadChunk.
	  We inject just after "setLoadedToWorld" is made false, since here the WorldChunk is guaranteed to be unloaded.
	 */
	/**
	 * @author Kasualix
	 * @reason insert event invoker
	 */
	@Overwrite
	private void tryUnloadChunk(long pos, ChunkHolder arg) {
		CompletableFuture<Chunk> completablefuture = arg.getSavingFuture();
		Consumer var10001 = (arg2) -> {
			CompletableFuture<Chunk> completablefuture1 = arg.getSavingFuture();
			if (completablefuture1 != completablefuture) {
				this.tryUnloadChunk(pos, arg);
			} else if (this.chunksToUnload.remove(pos, arg) && arg2 != null) {
				if (arg2 instanceof WorldChunk) {
					((WorldChunk)arg2).setLoadedToWorld(false);
					ServerChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, (WorldChunk) arg2);
					MinecraftForge.EVENT_BUS.post(new ChunkEvent.Unload((WorldChunk)arg2));
				}

				this.save((Chunk)arg2);
				if (this.loadedChunks.remove(pos) && arg2 instanceof WorldChunk) {
					WorldChunk chunk = (WorldChunk)arg2;
					this.world.unloadEntities(chunk);
				}

				((ExtendedServerLightingProvider)this.serverLightingProvider).frogeapi$updateChunkStatus(((Chunk)arg2).getPos());
				this.serverLightingProvider.tick();
				this.worldGenerationProgressListener.setChunkStatus(((Chunk)arg2).getPos(), null);
			}

		};
		Queue<Runnable> var10002 = this.unloadTaskQueue;
		var10002.getClass();
		completablefuture.thenAcceptAsync(var10001, var10002::add).whenComplete((void_, throwable) -> {
			if (throwable != null) {
				LOGGER.error("Failed to save chunk " + arg.getPos(), throwable);
			}
		});
	}


	/*
	  Injection is inside of convertToFullChunk?

	  <p>The following is expected contractually

	  <ul><li>the chunk being loaded MUST be a WorldChunk.
	  <li>everything within the chunk has been loaded into the world. Entities, BlockEntities, etc.</ul>
	 */
	/**
	 * @author Kasualix
	 * @reason insert event invoker
	 */
	@Overwrite
	private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> convertToFullChunk(ChunkHolder arg) {
		CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completablefuture = arg.getFutureFor(ChunkStatus.FULL.getPrevious());
		return completablefuture.thenApplyAsync((either) -> {
			ChunkStatus chunkstatus = ChunkHolder.getTargetStatusForLevel(arg.getLevel());
			return !chunkstatus.isAtLeast(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : either.mapLeft((arg2) -> {
				ChunkPos chunkpos = arg.getPos();
				WorldChunk chunk;
				if (arg2 instanceof ReadOnlyChunk) {
					chunk = ((ReadOnlyChunk)arg2).getWrappedChunk();
				} else {
					chunk = new WorldChunk(this.world, (ProtoChunk)arg2);
					arg.setCompletedChunk(new ReadOnlyChunk(chunk));
				}

				chunk.setLevelTypeProvider(() -> ChunkHolder.getLevelType(arg.getLevel()));
				chunk.loadToWorld();
				if (this.loadedChunks.add(chunkpos.toLong())) {
					chunk.setLoadedToWorld(true);

					try {
						((ExtendedChunkHolder)arg).setCurrentlyLoadingWorldChunk(chunk);
						this.world.addBlockEntities(chunk.getBlockEntities().values());
						List<Entity> list = null;
						TypeFilterableList<Entity>[] aclassinheritancemultimap = chunk.getEntitySectionArray();
						int i = aclassinheritancemultimap.length;

						for (TypeFilterableList<Entity> entities : aclassinheritancemultimap) {

							for (Entity entity : entities) {
								if (!(entity instanceof PlayerEntity) && !this.world.loadEntity(entity)) {
									if (list == null) {
										list = Lists.newArrayList(entity);
									} else {
										list.add(entity);
									}
								}
							}
						}
						if (list != null) {
							list.forEach(chunk::remove);
						}

						ServerChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.world, chunk);
						MinecraftForge.EVENT_BUS.post(new ChunkEvent.Load(chunk));
					} finally {
						((ExtendedChunkHolder)arg).setCurrentlyLoadingWorldChunk(null);
					}
				}

				return chunk;
			});
		}, (runnable) -> {
			MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> var10000 = this.mainExecutor;
			long var10002 = arg.getPos().toLong();
			arg.getClass();
			var10000.send(ChunkTaskPrioritySystem.createMessage(runnable, var10002, arg::getLevel));
		});
	}
}
