package net.casual.arcade.recipes

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe

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

    private class Wrapper<C: Container>(
        private val wrapped: Recipe<C>,
        predicate: PlayerPredicatedRecipe
    ): Recipe<C> by wrapped, PlayerPredicatedRecipe by predicate {
        override fun equals(other: Any?): Boolean {
            @Suppress("SuspiciousEqualsCombination")
            return this === other || this.wrapped == other
        }

        override fun hashCode(): Int {
            return this.wrapped.hashCode()
        }
    }

    public companion object {
        /**
         * This creates a wrapper recipe of the given [recipe]
         * with a given [predicate].
         *
         * @param recipe The recipe to wrap.
         * @param predicate The predicate to use.
         * @return The wrapped recipe.
         */
        public fun <C: Container> wrap(recipe: Recipe<C>, predicate: PlayerPredicatedRecipe): Recipe<C> {
            return Wrapper(recipe, predicate)
        }
    }
}