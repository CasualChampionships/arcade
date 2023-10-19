package net.casual.arcade.recipes

import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

public open class WrappedRecipe<C: Container>(
    public val wrapped: Recipe<C>
): Recipe<C> by wrapped {
    override fun getSerializer(): RecipeSerializer<Recipe<C>>? {
        return if (this.wrapped.serializer == null) null else WrappedRecipeSerializer.get()
    }

    override fun equals(other: Any?): Boolean {
        @Suppress("SuspiciousEqualsCombination")
        return this === other || this.wrapped == other
    }

    override fun hashCode(): Int {
        return this.wrapped.hashCode()
    }
}