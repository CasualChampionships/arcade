package net.casual.arcade.recipes

import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

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
        override fun getSerializer(): RecipeSerializer<Recipe<C>>? {
            return if (this.wrapped.serializer == null) null else WrapperRecipeSerializer.get()
        }

        override fun equals(other: Any?): Boolean {
            @Suppress("SuspiciousEqualsCombination")
            return this === other || this.wrapped == other
        }

        override fun hashCode(): Int {
            return this.wrapped.hashCode()
        }
    }

    private class WrapperRecipeSerializer<C: Container>: RecipeSerializer<Recipe<C>> {
        override fun fromJson(recipeId: ResourceLocation, serializedRecipe: JsonObject): Recipe<C> {
            throw UnsupportedOperationException()
        }

        override fun fromNetwork(recipeId: ResourceLocation, buffer: FriendlyByteBuf): Recipe<C> {
            throw UnsupportedOperationException()
        }

        override fun toNetwork(buffer: FriendlyByteBuf, recipe: Recipe<C>) {
            @Suppress("UNCHECKED_CAST")
            return (recipe.serializer as RecipeSerializer<Recipe<C>>).toNetwork(buffer, recipe)
        }

        companion object {
            private val INSTANCE = WrapperRecipeSerializer<Container>()

            fun <C: Container> get(): RecipeSerializer<Recipe<C>> {
                @Suppress("UNCHECKED_CAST")
                return INSTANCE as RecipeSerializer<Recipe<C>>
            }
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