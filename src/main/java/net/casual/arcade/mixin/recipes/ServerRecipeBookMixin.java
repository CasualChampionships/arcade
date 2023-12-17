package net.casual.arcade.mixin.recipes;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casual.arcade.Arcade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.ServerRecipeBook;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerRecipeBook.class)
public class ServerRecipeBookMixin {
	@WrapWithCondition(
		method = "loadRecipes",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V",
			ordinal = 0,
			remap = false
		)
	)
	private boolean onNoRecipeExists(Logger instance, String string, Object o) {
		return !Arcade.MOD_ID.equals(((ResourceLocation) o).getNamespace());
	}
}
