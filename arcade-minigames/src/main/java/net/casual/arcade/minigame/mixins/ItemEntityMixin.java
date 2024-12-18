/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
	@Inject(
		method = "playerTouch",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canPlayerPickUp(Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			Minigame minigame = MinigameUtils.getMinigame(serverPlayer);
			if (minigame != null && !minigame.getSettings().canPickupItems.get(serverPlayer)) {
				ci.cancel();
			}
		}
	}
}
