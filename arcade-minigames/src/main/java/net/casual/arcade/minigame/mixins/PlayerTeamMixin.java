/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.LinkedHashSet;

@Mixin(PlayerTeam.class)
public class PlayerTeamMixin {
	@ModifyExpressionValue(
		method = "<init>",
		at = @At(
			value = "NEW",
			target = "()Ljava/util/HashSet;"
		)
	)
	private <E> HashSet<E> repalceWithLinkedHashSet(HashSet<E> original) {
		// Iterate players in a consistent order
		return new LinkedHashSet<>(original);
	}
}
