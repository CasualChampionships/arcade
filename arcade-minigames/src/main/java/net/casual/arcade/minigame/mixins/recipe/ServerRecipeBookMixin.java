package net.casual.arcade.minigame.mixins.recipe;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.managers.MinigameRecipeManager;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.casual.arcade.utils.ArcadeUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;
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
			manager.grant(player, intersection);
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
			manager.revoke(player, intersection);
		});
	}

	@WrapWithCondition(
		method = "loadRecipes",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V",
			ordinal = 0,
			remap = false
		)
	)
	private boolean onNoRecipeExists(Logger instance, String string, Object o) {
		return !ArcadeUtils.MOD_ID.equals(((ResourceLocation) o).getNamespace());
	}

	@Unique
	private void extractMinigameRecipes(
		Collection<RecipeHolder<?>> recipes,
		ServerPlayer player,
		LocalRef<Collection<RecipeHolder<?>>> recipesRef,
		BiConsumer<MinigameRecipeManager, Collection<RecipeHolder<?>>> consumer
	) {
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		if (minigame == null) {
			return;
		}

		Set<RecipeHolder<?>> intersection = new HashSet<>(recipes);
		intersection.retainAll(minigame.getRecipes().all());
		if (!intersection.isEmpty()) {
			Set<RecipeHolder<?>> copy = new HashSet<>(recipes);
			copy.removeAll(intersection);
			recipesRef.set(copy);

			consumer.accept(minigame.getRecipes(), intersection);
		}
	}
}
