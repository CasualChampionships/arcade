package net.casual.arcade.ducks;

import net.casual.arcade.utils.ducks.MutableRecipeManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Arcade$MutableRecipeManager extends MutableRecipeManager {
	void arcade$addRecipes(Collection<? extends RecipeHolder<?>> recipes);

	void arcade$removeRecipes(Collection<? extends RecipeHolder<?>> recipes);

	@Override
	default void addRecipes(@NotNull Collection<? extends RecipeHolder<?>> recipes) {
		this.arcade$addRecipes(recipes);
	}

	@Override
	default void removeRecipes(@NotNull Collection<? extends RecipeHolder<?>> recipes) {
		this.arcade$removeRecipes(recipes);
	}
}
