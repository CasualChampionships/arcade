package net.casualuhc.arcade.recipes

import net.minecraft.world.item.crafting.Recipe

interface MutableRecipeManager {
    fun addRecipes(recipes: Collection<Recipe<*>>)
}