/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@WrapWithCondition(
		method = "createLevels",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;addWorldborderListener(Lnet/minecraft/server/level/ServerLevel;)V"
		)
	)
	private boolean onAddGlobalWorldBorderListener(PlayerList instance, ServerLevel level) {
		return false;
	}
}
