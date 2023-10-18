package net.casual.arcade.ducks;

import net.casual.arcade.utils.ducks.MutableRecipeManager;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Arcade$MutableRecipeManager extends MutableRecipeManager {
	void arcade$addRecipes(Collection<? extends Recipe<?>> recipes);

	void arcade$removeRecipes(Collection<? extends Recipe<?>> recipes);

	@Override
	default void addRecipes(@NotNull Collection<? extends Recipe<?>> recipes) {
		this.arcade$addRecipes(recipes);
	}

	@Override
	default void removeRecipes(@NotNull Collection<? extends Recipe<?>> recipes) {
		this.arcade$removeRecipes(recipes);
	}
}
