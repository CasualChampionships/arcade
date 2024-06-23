package net.casual.arcade.ducks;

import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;

public interface MutableRecipeManager {
	void arcade$addRecipes(Collection<? extends RecipeHolder<?>> recipes);

	void arcade$removeRecipes(Collection<? extends RecipeHolder<?>> recipes);
}
