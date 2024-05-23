package net.casual.arcade.recipes

import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer

public class WrappedRecipeSerializer<C: Container>(
    public val wrapped: RecipeSerializer<*>
): RecipeSerializer<Recipe<C>> {
    override fun codec(): MapCodec<Recipe<C>> {
        return wrapped.codec() as MapCodec<Recipe<C>>
    }

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, Recipe<C>> {
        return wrapped.streamCodec() as StreamCodec<RegistryFriendlyByteBuf, Recipe<C>>
    }
}