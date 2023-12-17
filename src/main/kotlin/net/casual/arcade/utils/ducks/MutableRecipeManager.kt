package net.casual.arcade.utils.ducks

import net.minecraft.world.item.crafting.RecipeHolder

public interface MutableRecipeManager {
    public fun addRecipes(recipes: Collection<RecipeHolder<*>>)

    public fun removeRecipes(recipes: Collection<RecipeHolder<*>>)
}