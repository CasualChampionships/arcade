package net.casual.arcade.recipes

import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level

public open class WrappedRecipe<C: Container>(
    public val wrapped: Recipe<C>
): Recipe<C> {
    override fun matches(container: C, level: Level): Boolean {
        return this.wrapped.matches(container, level)
    }

    override fun assemble(container: C, registries: HolderLookup.Provider): ItemStack {
        return this.wrapped.assemble(container, registries)
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return this.wrapped.canCraftInDimensions(width, height)
    }

    override fun getResultItem(registries: HolderLookup.Provider): ItemStack {
        return this.wrapped.getResultItem(registries)
    }

    override fun getRemainingItems(container: C): NonNullList<ItemStack> {
        return this.wrapped.getRemainingItems(container)
    }

    override fun getIngredients(): NonNullList<Ingredient> {
        return this.wrapped.ingredients
    }

    override fun isSpecial(): Boolean {
        return this.wrapped.isSpecial
    }

    override fun showNotification(): Boolean {
        return this.wrapped.showNotification()
    }

    override fun getGroup(): String {
        return this.wrapped.group
    }

    override fun getToastSymbol(): ItemStack {
        return this.wrapped.toastSymbol
    }

    override fun getSerializer(): RecipeSerializer<*> {
        return this.wrapped.serializer
    }

    override fun getType(): RecipeType<*> {
        return this.wrapped.type
    }

    override fun isIncomplete(): Boolean {
        return this.wrapped.isIncomplete
    }

    override fun equals(other: Any?): Boolean {
        @Suppress("SuspiciousEqualsCombination")
        return this === other || this.wrapped == other
    }

    override fun hashCode(): Int {
        return this.wrapped.hashCode()
    }
}