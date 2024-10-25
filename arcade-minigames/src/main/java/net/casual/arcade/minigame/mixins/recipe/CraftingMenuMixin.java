package net.casual.arcade.minigame.mixins.recipe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.crafting.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
	@ModifyExpressionValue(
		method = "slotChangedCraftingGrid",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;"
		)
	)
	@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unchecked"})
	private static <T extends Recipe<?>> Optional<RecipeHolder<T>> onGetRecipe(
		Optional<RecipeHolder<T>> original,
		@Local ServerPlayer player,
		@Local CraftingInput input,
		@Local(argsOnly = true) ServerLevel level
	) {
		if (original.isPresent()) {
			return original;
		}
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		if (minigame == null) {
			return original;
		}
		Optional<RecipeHolder<CraftingRecipe>> recipe = minigame.getRecipes().find(RecipeType.CRAFTING, input, level);
		return recipe.map(x -> (RecipeHolder<T>) x);
	}
}
