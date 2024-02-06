package net.casual.arcade.utils

import net.casual.arcade.ducks.`Arcade$MutableRecipeManager`
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager

public object RecipeUtils {
    public fun RecipeManager.addRecipes(recipes: Collection<RecipeHolder<*>>) {
        (this as `Arcade$MutableRecipeManager`).`arcade$addRecipes`(recipes)
    }

    public fun RecipeManager.removeRecipes(recipes: Collection<RecipeHolder<*>>) {
        (this as `Arcade$MutableRecipeManager`).`arcade$removeRecipes`(recipes)
    }
}