package net.casual.arcade.recipes

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CustomRecipe
import net.minecraft.world.item.crafting.RecipeSerializer

abstract class ArcadeCustomRecipe(
    id: ResourceLocation,
    category: CraftingBookCategory
): CustomRecipe(id, category) {
    final override fun getSerializer(): RecipeSerializer<*>? {
        return null
    }
}