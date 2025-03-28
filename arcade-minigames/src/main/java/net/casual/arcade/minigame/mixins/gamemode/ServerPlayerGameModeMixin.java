/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.gamemode;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.gamemode.ExtendedGameMode;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.fromVanilla;
import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow @Final protected ServerPlayer player;

	@ModifyExpressionValue(
		method = "changeGameModeForPlayer",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;gameModeForPlayer:Lnet/minecraft/world/level/GameType;"
		)
	)
	private GameType currentGameType(GameType original) {
		return null;
	}

	@Inject(
		method = "changeGameModeForPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V"
		),
		cancellable = true
	)
	private void onChangeGameMode(GameType gameModeForPlayer, CallbackInfoReturnable<Boolean> cir) {
		if (fromVanilla(gameModeForPlayer) == getExtendedGameMode(this.player)) {
			cir.setReturnValue(false);
			return;
		}
		ExtendedGameMode.setGameModeFromVanilla(this.player, gameModeForPlayer);
	}

	@ModifyExpressionValue(
		method = {"useItemOn", "useItem"},
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;gameModeForPlayer:Lnet/minecraft/world/level/GameType;"
		)
	)
	private GameType onGetSpectator(GameType original, ServerPlayer player) {
		if (getExtendedGameMode(player) == ExtendedGameMode.AdventureSpectator) {
			PlayerUtils.updateSelectedSlot(player);
			return GameType.SPECTATOR;
		}
		return original;
	}
}
