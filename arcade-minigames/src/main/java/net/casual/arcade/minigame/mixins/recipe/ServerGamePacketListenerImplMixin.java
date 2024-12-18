/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.recipe;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@WrapOperation(
		method = "handlePlaceRecipe",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/crafting/RecipeManager;getRecipeFromDisplay(Lnet/minecraft/world/item/crafting/display/RecipeDisplayId;)Lnet/minecraft/world/item/crafting/RecipeManager$ServerDisplayInfo;"
		)
	)
	private RecipeManager.ServerDisplayInfo onModifyRecipe(
		RecipeManager instance,
		RecipeDisplayId id,
		Operation<RecipeManager.ServerDisplayInfo> original
	) {
		if (id.index() < 0) {
			Minigame minigame = MinigameUtils.getMinigame(this.player);
			if (minigame != null) {
				return minigame.getRecipes().getHolderWithEntry(id.index());
			}
		}
		return original.call(instance, id);
	}

	@ModifyExpressionValue(
		method = "handlePlaceRecipe",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/stats/ServerRecipeBook;contains(Lnet/minecraft/resources/ResourceKey;)Z"
		)
	)
	private boolean doesPlayerHaveRecipe(boolean original, @Local RecipeHolder<?> holder) {
		if (original) {
			return true;
		}
		Minigame minigame = MinigameUtils.getMinigame(this.player);
		return minigame != null && minigame.getRecipes().has(this.player, holder.id());
	}
}
