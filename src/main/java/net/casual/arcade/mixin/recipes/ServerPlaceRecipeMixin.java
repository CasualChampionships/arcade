package net.casual.arcade.mixin.recipes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlaceRecipe.class)
public class ServerPlaceRecipeMixin {
	@ModifyExpressionValue(
		method = "recipeClicked",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/stats/ServerRecipeBook;contains(Lnet/minecraft/world/item/crafting/RecipeHolder;)Z"
		)
	)
	private boolean doesPlayerHaveRecipe(boolean original, ServerPlayer player, RecipeHolder<?> recipe) {
		if (original) {
			return true;
		}
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		return minigame != null && minigame.getRecipes().has(player, recipe);
	}
}
