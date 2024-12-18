/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.gamemode;

import net.casual.arcade.minigame.gamemode.ExtendedGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(
		method = "isInvisible",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onIsInvisible(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			if (getExtendedGameMode(player) == ExtendedGameMode.AdventureSpectator) {
				cir.setReturnValue(true);
			}
		}
	}
}
