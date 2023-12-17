package net.casual.arcade.utils

import net.casual.arcade.utils.ducks.MutableRecipeManager
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager

public object RecipeUtils {
    public fun RecipeManager.addRecipes(recipes: Collection<RecipeHolder<*>>) {
        (this as MutableRecipeManager).addRecipes(recipes)
    }

    public fun RecipeManager.removeRecipes(recipes: Collection<RecipeHolder<*>>) {
        (this as MutableRecipeManager).removeRecipes(recipes)
    }
}