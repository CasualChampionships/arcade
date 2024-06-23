package net.casual.arcade.utils

import net.casual.arcade.ducks.MutableRecipeManager
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager

public object RecipeUtils {
    public fun RecipeManager.addRecipes(recipes: Collection<RecipeHolder<*>>) {
        (this as MutableRecipeManager).`arcade$addRecipes`(recipes)
    }

    public fun RecipeManager.removeRecipes(recipes: Collection<RecipeHolder<*>>) {
        (this as MutableRecipeManager).`arcade$removeRecipes`(recipes)
    }
}