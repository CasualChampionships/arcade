/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@ModifyReturnValue(
		method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;",
		at = @At("RETURN")
	)
	private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> modifyRecipe(
		Optional<RecipeHolder<T>> original,
		RecipeType<T> type,
		I input,
		Level level
	) {
		if (original.isPresent() || !(level instanceof ServerLevel serverLevel)) {
			return original;
		}

		for (Minigame minigame : MinigameUtils.getMinigames(serverLevel)) {
			Optional<RecipeHolder<T>> recipe = minigame.getRecipes().find(type, input, serverLevel);
			if (recipe.isPresent()) {
				return recipe;
			}
		}
		return original;
 	}
}
