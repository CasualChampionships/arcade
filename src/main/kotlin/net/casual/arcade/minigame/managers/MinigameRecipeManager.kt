package net.casual.arcade.minigame.managers

import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.server.ServerRecipeReloadEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.recipes.PlayerPredicatedRecipe
import net.casual.arcade.recipes.WrappedRecipe
import net.casual.arcade.utils.RecipeUtils.addRecipes
import net.casual.arcade.utils.RecipeUtils.removeRecipes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.*

/**
 * This class manages the recipes of a minigame.
 *
 * All recipes added to this manager are local to the
 * minigame only and do not exist outside the context
 * of the minigame.
 *
 * @see Minigame.recipes
 */
public class MinigameRecipeManager(
    private val minigame: Minigame<*>
) {
    private val recipes = ArrayList<RecipeHolder<*>>()

    init {
        this.minigame.events.register<ServerRecipeReloadEvent> {
            it.addAll(this.recipes)
        }
        this.minigame.events.register<MinigameCloseEvent> {
            this.removeAll()
        }
    }

    @Deprecated("Use addAll instead", ReplaceWith("this.addAll(recipes)"))
    public fun add(recipes: Collection<RecipeHolder<*>>) {
        this.addAll(recipes)
    }

    public fun add(recipe: RecipeHolder<*>) {
        if (this.recipes.add(this.wrap(recipe))) {
            this.minigame.server.recipeManager.addRecipes(listOf(recipe))
        }
    }

    public fun addAll(recipes: Collection<RecipeHolder<*>>) {
        val minigameRecipes = recipes.map { this.wrap(it) }
        if (this.recipes.addAll(minigameRecipes)) {
            this.minigame.server.recipeManager.addRecipes(minigameRecipes)
        }
    }

    public fun remove(recipe: RecipeHolder<*>) {
        if (this.recipes.remove(recipe)) {
            this.minigame.server.recipeManager.removeRecipes(listOf(recipe))
        }
    }

    @Deprecated("Use removeAll instead", ReplaceWith("this.removeAll(recipes)"))
    public fun remove(recipes: Collection<RecipeHolder<*>>) {
        this.removeAll(recipes)
    }

    public fun removeAll(recipes: Collection<RecipeHolder<*>>) {
        if (this.recipes.removeAll(recipes.toSet())) {
            this.minigame.server.recipeManager.removeRecipes(recipes)
        }
    }

    public fun removeAll() {
        this.minigame.server.recipeManager.removeRecipes(this.recipes)
        this.recipes.clear()
    }

    public fun all(): Collection<RecipeHolder<*>> {
        return this.recipes
    }

    private fun wrap(holder: RecipeHolder<*>): RecipeHolder<*> {
        val wrapper = when (val recipe = holder.value) {
            is CraftingRecipe -> {
                object: MinigameRecipe<CraftingContainer>(recipe), CraftingRecipe {
                    override fun getType(): RecipeType<*> {
                        return RecipeType.CRAFTING
                    }

                    override fun category(): CraftingBookCategory {
                        return recipe.category()
                    }
                }
            }
            else -> MinigameRecipe(recipe)
        }
        return RecipeHolder(
            Arcade.id("${this.minigame.uuid}.${holder.id.path}.${holder.id.namespace}"),
            wrapper
        )
    }

    private open inner class MinigameRecipe<C: Container>(
        wrapped: Recipe<C>
    ): WrappedRecipe<C>(wrapped), PlayerPredicatedRecipe {
        override fun canUse(player: ServerPlayer): Boolean {
            return minigame.players.has(player) &&
                ((this.wrapped !is PlayerPredicatedRecipe) || this.wrapped.canUse(player))
        }
    }
}