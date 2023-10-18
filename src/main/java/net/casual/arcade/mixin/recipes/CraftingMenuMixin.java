package net.casual.arcade.mixin.recipes;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.recipes.PlayerPredicatedRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin {
	@ModifyReceiver(
		method = "slotChangedCraftingGrid",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/ResultContainer;setRecipeUsed(Lnet/minecraft/world/level/Level;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/Recipe;)Z"
		)
	)
	private static boolean onGetRecipe(
		boolean original,
		@Local Player player,
		@Local CraftingRecipe craftingRecipe
	) {
		if (original) {
			if (craftingRecipe instanceof PlayerPredicatedRecipe predicated) {
				return predicated.canUse((ServerPlayer) player);
			}
			return true;
		}
		return false;
	}
}
