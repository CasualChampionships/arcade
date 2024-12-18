/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Shadow @Final public MinecraftServer server;

	@ModifyExpressionValue(
		method = "isPvpAllowed",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;isPvpAllowed()Z"
		)
	)
	private boolean isPvpAllowed(boolean original) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame minigame = MinigameUtils.getMinigame(player);
		return original && (minigame == null || minigame.getSettings().canPvp.get(player));
	}

	@Inject(
		method = "isInvulnerableTo",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onIsInvulnerableTo(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canTakeDamage.get(player)) {
			if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(
		method = "drop(Z)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onDropItem(boolean dropStack, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canDropItems.get(player)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyExpressionValue(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;",
			ordinal = 1
		)
	)
	private BlockPos getSharedSpawnPosition(BlockPos original) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		ServerPlayer old = this.server.getPlayerList().getPlayer(player.getUUID());
		if (old == null) {
			return original;
		}
		Minigame minigame = MinigameUtils.getMinigame(old);
		if (minigame != null) {
			BlockPos spawnPosition = minigame.getLevels().getSpawn().position(player);
			if (spawnPosition != null) {
				return spawnPosition;
			}
		}
		return original;
	}

	@ModifyExpressionValue(
		method = "findRespawnPositionAndUseSpawnBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"
		)
	)
	private ServerLevel getDefaultRespawnDimension(ServerLevel original) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame != null) {
			ServerLevel spawn = minigame.getLevels().getSpawn().level(player);
			if (spawn != null) {
				return spawn;
			}
		}
		return original;
	}
}
