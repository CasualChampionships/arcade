package net.casualuhc.arcade.recipes

import com.google.gson.JsonObject
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

@Deprecated("Rework needed")
object ArcadeRecipeSerializer: RecipeSerializer<Recipe<Container>> {
    override fun fromJson(recipeId: ResourceLocation, serializedRecipe: JsonObject): Recipe<Container> {
        throw IllegalStateException()
    }

    override fun fromNetwork(recipeId: ResourceLocation, buffer: FriendlyByteBuf): Recipe<Container> {
        throw IllegalStateException()
    }

    override fun toNetwork(buffer: FriendlyByteBuf, recipe: Recipe<Container>) {
        throw IllegalStateException()
    }
}