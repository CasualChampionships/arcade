package net.casual.arcade.recipes

import eu.pb4.polymer.core.api.item.PolymerRecipe
import net.minecraft.core.HolderLookup
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This class is the superclass of all custom server-side
 * crafting recipes.
 *
 * You should implement this class **only** if your recipe
 * requires using the nbt from the item stacks, otherwise
 * you should either implement your recipe with a datapack
 * or using [ShapedRecipe] or [ShapelessRecipe], to do this
 * using code you may use [CraftingRecipeBuilder].
 *
 * These recipes **will not** be displayed in the player's
 * crafting book as they cannot be properly serialized to
 * the players.
 *
 * @param category The category of the recipe.
 * @see CraftingRecipeBuilder
 */
public abstract class ArcadeCustomRecipe(
    category: CraftingBookCategory
): CustomRecipe(category), PolymerRecipe {
    /**
     * This method is called when the recipe is being assembled.
     *
     * You have access to all the items in the crafting container
     * allowing you to determine what the output item is.
     *
     * @param input The crafting container holding the recipe items.
     * @param access Access to all the registries.
     * @return The item being crafted.
     */
    abstract override fun assemble(input: CraftingInput, access: HolderLookup.Provider): ItemStack

    /**
     * This method is called to see if this recipe is able to fit
     * in a given crafting container of [width] and [height].
     *
     * @param width The width of the crafting container.
     * @param height The height of the crafting container.
     * @return Whether the recipe fits.
     */
    abstract override fun canCraftInDimensions(width: Int, height: Int): Boolean

    /**
     * This method is used to check whether the recipe matches
     * what is currently in a crafting container.
     *
     * This method is called only if [canCraftInDimensions] returns `true`.
     *
     * @param input The crafting container to match against.
     * @param level The level the player is currently in.
     * @return Whether the recipe is a match.
     */
    @OverrideOnly
    protected abstract fun isMatch(input: CraftingInput, level: ServerLevel): Boolean

    /**
     * This method is used to check whether the recipe matches
     * what is currently in a crafting container.
     *
     * @param input The crafting container to match against.
     * @param level The level the player is currently in.
     * @return Whether the recipe is a match.
     * @see isMatch
     */
    final override fun matches(input: CraftingInput, level: Level): Boolean {
        return this.canCraftInDimensions(input.width(), input.height()) && this.isMatch(input, level as ServerLevel)
    }

    /**
     * We cannot serialize a server-side only recipe to
     * the client, this method will always return null.
     *
     * @return `null`
     */
    final override fun getSerializer(): RecipeSerializer<*>? {
        return null
    }
}