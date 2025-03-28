/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.recipe;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.managers.MinigameRecipeManager;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin {
	@Inject(
		method = "addRecipes",
		at = @At("HEAD")
	)
	private void onAddRecipes(
		Collection<RecipeHolder<?>> recipes,
		ServerPlayer player,
		CallbackInfoReturnable<Integer> cir,
		@Local(argsOnly = true) LocalRef<Collection<RecipeHolder<?>>> recipesRef
	) {
		this.extractMinigameRecipes(recipes, player, recipesRef, (manager, intersection) -> {
			manager.grantAll(player, intersection, false);
		});
	}

	@Inject(
		method = "removeRecipes",
		at = @At("HEAD")
	)
	private void onRemoveRecipes(
		Collection<RecipeHolder<?>> recipes,
		ServerPlayer player,
		CallbackInfoReturnable<Integer> cir,
		@Local(argsOnly = true) LocalRef<Collection<RecipeHolder<?>>> recipesRef
	) {
		this.extractMinigameRecipes(recipes, player, recipesRef, (manager, intersection) -> {
			manager.revokeAll(player, intersection);
		});
	}

	@Unique
	private void extractMinigameRecipes(
		Collection<RecipeHolder<?>> recipes,
		ServerPlayer player,
		LocalRef<Collection<RecipeHolder<?>>> recipesRef,
		BiConsumer<MinigameRecipeManager, Collection<ResourceKey<Recipe<?>>>> consumer
	) {
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame == null) {
			return;
		}

		Set<RecipeHolder<?>> intersection = new HashSet<>(recipes);
		intersection.retainAll(minigame.getRecipes().all());
		if (!intersection.isEmpty()) {
			Set<RecipeHolder<?>> copy = new HashSet<>(recipes);
			copy.removeAll(intersection);
			recipesRef.set(copy);
			consumer.accept(minigame.getRecipes(), intersection.stream().map(RecipeHolder::id).toList());
		}
	}
}
