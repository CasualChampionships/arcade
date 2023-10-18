package net.casual.arcade.utils.ducks

import net.minecraft.world.item.crafting.Recipe

public interface MutableRecipeManager {
    public fun addRecipes(recipes: Collection<Recipe<*>>)

    public fun removeRecipes(recipes: Collection<Recipe<*>>)
}