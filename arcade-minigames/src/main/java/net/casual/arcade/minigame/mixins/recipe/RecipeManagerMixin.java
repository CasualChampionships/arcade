package net.casual.arcade.minigame.mixins.recipe;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.casual.arcade.utils.impl.ConcatenatedList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.*;

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
		Minigame<?> minigame = MinigameUtils.getMinigame(serverLevel);
		if (minigame == null) {
			return original;
		}

		return minigame.getRecipes().find(type, input, serverLevel);
 	}
}
