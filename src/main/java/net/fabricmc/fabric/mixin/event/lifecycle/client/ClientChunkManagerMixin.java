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

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientChunkManager.class)
public abstract class ClientChunkManagerMixin {
	@Final
	@Shadow
	private ClientWorld world;

	@Inject(method = "loadChunkFromPacket",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z",
					shift = At.Shift.BEFORE,
					remap = false
			)
	)
	private void onChunkLoad(int chunkX, int chunkZ, BiomeArray biomes, PacketByteBuf buf, CompoundTag tag, int k, boolean complete, CallbackInfoReturnable<WorldChunk> cir) {
		ClientChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.world, cir.getReturnValue());
	}

	@Inject(
			method = "unload" ,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z",
					shift = At.Shift.BEFORE,
					remap = false
			)
	)
	private void onChunkUnload(int chunkX, int chunkZ, CallbackInfo ci, WorldChunk chunk) {
		ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.world, chunk);
	}
}