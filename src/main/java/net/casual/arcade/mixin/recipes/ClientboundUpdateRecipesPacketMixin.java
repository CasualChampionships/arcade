package net.casual.arcade.mixin.recipes;

import net.casual.arcade.recipes.WrappedRecipe;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ClientboundUpdateRecipesPacket.class)
public class ClientboundUpdateRecipesPacketMixin {
	@Redirect(
		method = "<init>(Ljava/util/Collection;)V",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;copyOf(Ljava/util/Collection;)Ljava/util/List;",
			remap = false
		)
	)
	private List<RecipeHolder<?>> onSetRecipes(Collection<RecipeHolder<?>> recipes) {
		// Our recipes have no serializer!
		ArrayList<RecipeHolder<?>> copy = new ArrayList<>();
		for (RecipeHolder<?> recipe : recipes) {
			if (recipe.value() instanceof WrappedRecipe<?> wrapped) {
				copy.add(new RecipeHolder<>(recipe.id(), wrapped));
			}
		}
		return copy;
	}
}
