package net.casual.arcade.recipes

import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe

public open class WrappedRecipe<C: Container>(
    public val wrapped: Recipe<C>
): Recipe<C> by wrapped {
    override fun equals(other: Any?): Boolean {
        @Suppress("SuspiciousEqualsCombination")
        return this === other || this.wrapped == other
    }

    override fun hashCode(): Int {
        return this.wrapped.hashCode()
    }
}