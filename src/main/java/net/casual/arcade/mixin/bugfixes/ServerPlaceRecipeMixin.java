package net.casual.arcade.mixin.bugfixes;

import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlaceRecipe.class)
public class ServerPlaceRecipeMixin {
	// Fixes an issue where recipes require an exact component match,
	// although the required stack always has no components
	@Redirect(
		method = "moveItemToGrid",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingUnusedItem(Lnet/minecraft/world/item/ItemStack;)I"
		)
	)
	private int onFindUnusedItem(Inventory instance, ItemStack required) {
		if (required.isEmpty()) {
			return -1;
		}

		for (int i = 0; i < instance.items.size(); i++) {
			ItemStack stack = instance.items.get(i);
			if (ItemStack.isSameItem(required, stack) && !stack.isDamaged() && !stack.isEnchanted()) {
				return i;
			}
		}

		return -1;
	}
}
