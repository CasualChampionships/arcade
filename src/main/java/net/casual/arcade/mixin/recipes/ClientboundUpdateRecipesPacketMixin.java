package net.casual.arcade.mixin.recipes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;

@Mixin(ClientboundUpdateRecipesPacket.class)
public class ClientboundUpdateRecipesPacketMixin {
	@ModifyExpressionValue(
		method = "<init>(Ljava/util/Collection;)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Lists;newArrayList(Ljava/lang/Iterable;)Ljava/util/ArrayList;",
			remap = false
		)
	)
	@SuppressWarnings("ConstantValue")
	private ArrayList<RecipeHolder<?>> onSetRecipes(ArrayList<RecipeHolder<?>> recipes) {
		// Our recipes have no serializer!
		recipes.removeIf(recipe -> recipe.value().getSerializer() == null);
		return recipes;
	}
}
