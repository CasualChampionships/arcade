/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.gamemode;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.minigame.gamemode.ExtendedGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Shadow public abstract boolean isSpectator();

	@Inject(
		method = "broadcastToPlayer",
		at = @At("HEAD"),
		cancellable = true
	)
	private void shouldBroadcastTo(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
		if (getExtendedGameMode((ServerPlayer) (Object) this) == ExtendedGameMode.AdventureSpectator) {
			cir.setReturnValue(false);
		}
	}

	@Inject(
		method = "checkFallDamage",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onCheckFallDamage(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
		// Prevent fall damage particles if in AdventureSpectator
		if (getExtendedGameMode((ServerPlayer) (Object) this) == ExtendedGameMode.AdventureSpectator) {
			ci.cancel();
		}
	}

	@Inject(
		method = "isSpectator",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onIsSpectator(CallbackInfoReturnable<Boolean> cir) {
		if (getExtendedGameMode((ServerPlayer) (Object) this) == ExtendedGameMode.AdventureSpectator) {
			cir.setReturnValue(true);
		}
	}

	@ModifyExpressionValue(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;getGameModeForPlayer()Lnet/minecraft/world/level/GameType;"
		)
	)
	private GameType onGetSpectator(GameType original) {
		return this.isSpectator() ? GameType.SPECTATOR : original;
	}

	@ModifyExpressionValue(
		method = "storeGameTypes",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;getGameModeForPlayer()Lnet/minecraft/world/level/GameType;"
		)
	)
	private GameType getGameTypeForPlayer(GameType original) {
		if (getExtendedGameMode((ServerPlayer) (Object) this) == ExtendedGameMode.AdventureSpectator) {
			// When the player re-joins, they'll appear in spectator.
			return GameType.SPECTATOR;
		}
		return original;
	}

	@WrapWithCondition(
		method = "updateInvisibilityStatus",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;setInvisible(Z)V"
		)
	)
	private boolean shouldUpdateInvisible(ServerPlayer instance, boolean b) {
		return getExtendedGameMode((ServerPlayer) (Object) this) != ExtendedGameMode.AdventureSpectator;
	}
}
