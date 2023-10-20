package net.casual.arcade.recipes

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType

/**
 * This interface is used to determine whether a recipe
 * can be crafted by a given player.
 */
public fun interface PlayerPredicatedRecipe {
    /**
     * This method is used to check whether a player can
     * craft this recipe.
     *
     * @param player The player to check.
     * @return Whether the player can craft this recipe.
     */
    public fun canUse(player: ServerPlayer): Boolean

    public companion object {
        /**
         * This creates a wrapper recipe of the given [recipe]
         * with a given [predicate].
         *
         * @param recipe The recipe to wrap.
         * @param predicate The predicate to use.
         * @return The wrapped recipe.
         */
        public fun <C: Container> wrap(recipe: Recipe<C>, predicate: PlayerPredicatedRecipe): Recipe<*> {
            if (recipe is CraftingRecipe) {
                return object: WrappedRecipe<CraftingContainer>(recipe), CraftingRecipe, PlayerPredicatedRecipe by predicate {
                    override fun getType(): RecipeType<*> {
                        return RecipeType.CRAFTING
                    }

                    override fun category(): CraftingBookCategory {
                        return recipe.category()
                    }
                }
            }
            return object: WrappedRecipe<C>(recipe), PlayerPredicatedRecipe by predicate { }
        }
    }
}