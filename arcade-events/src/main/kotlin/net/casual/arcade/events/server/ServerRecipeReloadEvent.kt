package net.casual.arcade.events.server

import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeManager
import java.util.*

@Deprecated("Use the minigame recipe manager instead!")
public data class ServerRecipeReloadEvent(
    val recipeManager: RecipeManager,
    val resourceManager: ResourceManager
): SafeServerlessEvent {
    private val recipes = LinkedList<RecipeHolder<*>>()

    public fun add(recipe: RecipeHolder<*>) {
        this.recipes.add(recipe)
    }

    public fun addAll(recipes: Collection<RecipeHolder<*>>) {
        this.recipes.addAll(recipes)
    }

    public fun getRecipes(): List<RecipeHolder<*>> {
        return this.recipes
    }
}