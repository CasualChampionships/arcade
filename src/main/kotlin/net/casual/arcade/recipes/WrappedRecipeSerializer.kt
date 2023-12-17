package net.casual.arcade.recipes

import com.mojang.serialization.Codec
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

public class WrappedRecipeSerializer<C: Container>(
    public val wrapped: RecipeSerializer<*>
): RecipeSerializer<WrappedRecipe<C>> {
    override fun codec(): Codec<WrappedRecipe<C>>? {
        return null
    }

    override fun fromNetwork(buffer: FriendlyByteBuf): WrappedRecipe<C>? {
        return null
    }

    override fun toNetwork(buffer: FriendlyByteBuf, recipe: WrappedRecipe<C>) {
        @Suppress("UNCHECKED_CAST")
        return (recipe.wrapped.serializer as RecipeSerializer<Recipe<C>>).toNetwork(buffer, recipe.wrapped)
    }
}