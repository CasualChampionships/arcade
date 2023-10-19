package net.casual.arcade.recipes

import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

public class WrappedRecipeSerializer<C: Container>(
    public val wrapped: RecipeSerializer<*>
): RecipeSerializer<WrappedRecipe<C>> {
    override fun fromJson(recipeId: ResourceLocation, serializedRecipe: JsonObject): WrappedRecipe<C> {
        throw UnsupportedOperationException()
    }

    override fun fromNetwork(recipeId: ResourceLocation, buffer: FriendlyByteBuf): WrappedRecipe<C> {
        throw UnsupportedOperationException()
    }

    override fun toNetwork(buffer: FriendlyByteBuf, recipe: WrappedRecipe<C>) {
        @Suppress("UNCHECKED_CAST")
        return (recipe.wrapped.serializer as RecipeSerializer<Recipe<C>>).toNetwork(buffer, recipe.wrapped)
    }
}