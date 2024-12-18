/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.gamemode;

import net.casual.arcade.minigame.gamemode.ExtendedGameMode;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(
		method = "interactOn",
		at = @At("HEAD")
	)
	private void onInteractOn(
		Entity entity,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		if ((Object) this instanceof ServerPlayer player) {
			if (getExtendedGameMode(player) == ExtendedGameMode.AdventureSpectator) {
				PlayerUtils.updateSelectedSlot(player);
			}
		}
	}
}
