package net.casual.arcade.minigame.managers

import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.server.ServerRecipeReloadEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.recipes.PlayerPredicatedRecipe
import net.casual.arcade.recipes.WrappedRecipe
import net.casual.arcade.utils.RecipeUtils.addRecipes
import net.casual.arcade.utils.RecipeUtils.removeRecipes
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType

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
        val minigameRecipes = recipes.map { this.wrap(it) }
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

    private fun <C: Container> wrap(recipe: Recipe<C>): Recipe<*> {
        if (recipe is CraftingRecipe) {
            return object: MinigameRecipe<CraftingContainer>(recipe), CraftingRecipe {
                override fun getType(): RecipeType<*> {
                    return RecipeType.CRAFTING
                }

                override fun category(): CraftingBookCategory {
                    return recipe.category()
                }
            }
        }
        return MinigameRecipe(recipe)
    }

    private open inner class MinigameRecipe<C: Container>(
        wrapped: Recipe<C>
    ): WrappedRecipe<C>(wrapped), PlayerPredicatedRecipe {
        override fun getId(): ResourceLocation {
            return Arcade.id("${minigame.uuid}.${wrapped.id.path}.${wrapped.id.namespace}")
        }

        override fun canUse(player: ServerPlayer): Boolean {
            return minigame.hasPlayer(player)
        }
    }
}