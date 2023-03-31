package net.casualuhc.arcade.recipes

import net.casualuhc.arcade.Arcade
import net.minecraft.world.item.crafting.Recipe
import java.util.function.Consumer

object RecipeHandler {
    private val customRecipes = LinkedHashSet<Recipe<*>>()

    @JvmStatic
    fun register(recipe: Recipe<*>) {
        this.customRecipes.add(recipe)
        (Arcade.server.recipeManager as MutableRecipeManager).addRecipes(listOf(recipe))
    }

    @JvmStatic
    fun forEachCustom(consumer: Consumer<Recipe<*>>) {
        this.customRecipes.forEach(consumer)
    }
}