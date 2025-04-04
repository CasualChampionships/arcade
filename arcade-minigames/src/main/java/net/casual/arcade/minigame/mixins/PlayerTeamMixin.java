/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.LinkedHashSet;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;",
			remap = false
		)
	)
	private <E> HashSet<E> onNewHashSet() {
		// Iterate players in a consistent order
		return new LinkedHashSet<>();
	}
}
