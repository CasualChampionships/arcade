package net.casual.arcade.mixin.recipes;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.recipes.ArcadeCustomRecipe;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.Recipe;
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
	private ArrayList<Recipe<?>> onSetRecipes(ArrayList<Recipe<?>> recipes) {
		// Our recipes have no serializer!
		recipes.removeIf(recipe -> recipe.getSerializer() == null);
		return recipes;
	}
}
