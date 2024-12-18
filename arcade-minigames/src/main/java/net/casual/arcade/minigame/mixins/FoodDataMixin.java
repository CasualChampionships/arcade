/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodData.class)
public class FoodDataMixin {
	@WrapWithCondition(
		method = "tick",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Ljava/lang/Math;max(II)I"
		)
	)
	private boolean onDecreaseFood(FoodData instance, int value, ServerPlayer player) {
		Minigame minigame = MinigameUtils.getMinigame(player);
		return minigame == null || minigame.getSettings().canGetHungry.get(player);
	}
}
