package net.casual.arcade.minigame.managers

import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.server.ServerRecipeReloadEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.recipes.PlayerPredicatedRecipe
import net.casual.arcade.utils.RecipeUtils.addRecipes
import net.casual.arcade.utils.RecipeUtils.removeRecipes
import net.minecraft.world.item.crafting.Recipe

public class MinigameRecipeManager(
    private val minigame: Minigame<*>
) {
    private val recipes = ArrayList<Recipe<*>>()

    init {
        this.minigame.events.register<ServerRecipeReloadEvent> {
            it.addAll(this.recipes)
        }
        this.minigame.events.register<MinigameCloseEvent> {
            this.removeAll()
        }
    }

    public fun add(recipes: Collection<Recipe<*>>) {
        val minigameRecipes = recipes.map { PlayerPredicatedRecipe.wrap(it, this.minigame::hasPlayer) }
        if (this.recipes.addAll(minigameRecipes)) {
            this.minigame.server.recipeManager.addRecipes(minigameRecipes)
        }
    }

    public fun remove(recipes: Collection<Recipe<*>>) {
        if (this.recipes.removeAll(recipes.toSet())) {
            this.minigame.server.recipeManager.removeRecipes(recipes)
        }
    }

    public fun removeAll() {
        this.minigame.server.recipeManager.removeRecipes(this.recipes)
        this.recipes.clear()
    }
}