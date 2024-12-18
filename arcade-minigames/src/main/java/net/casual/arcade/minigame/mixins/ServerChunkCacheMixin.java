/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
	@Shadow @Final private ServerLevel level;

	@ModifyExpressionValue(
		method = "tickChunks()V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/TickRateManager;runsNormally()Z"
		)
	)
	private boolean isTicking(boolean original) {
		return MinigameUtils.isTicking(this.level);
	}
}
