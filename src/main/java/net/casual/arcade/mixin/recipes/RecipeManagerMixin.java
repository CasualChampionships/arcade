package net.casual.arcade.mixin.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.ducks.Arcade$MutableRecipeManager;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ServerRecipeReloadEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements Arcade$MutableRecipeManager {
	@Shadow private Map<ResourceLocation, RecipeHolder<?>> byName;

	@Shadow public abstract void replaceRecipes(Iterable<RecipeHolder<?>> recipes);

	@Inject(
		method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;entrySet()Ljava/util/Set;",
			ordinal = 0,
			shift = At.Shift.BEFORE,
			remap = false
		)
	)
	private void onReloadRecipes(
		Map<ResourceLocation, JsonElement> object,
		ResourceManager resourceManager,
		ProfilerFiller profiler,
		CallbackInfo ci,
		@Local(ordinal = 1) Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>>> recipesByType,
		@Local ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> recipesByName
	) {
		ServerRecipeReloadEvent event = new ServerRecipeReloadEvent((RecipeManager) (Object) this, resourceManager);
		GlobalEventHandler.broadcast(event);

		for (RecipeHolder<?> recipe : event.getRecipes()) {
			recipesByType.computeIfAbsent(recipe.value().getType(), recipeType -> ImmutableMap.builder()).put(recipe.id(), recipe);
			recipesByName.put(recipe.id(), recipe);
		}
	}

	@Override
	public void arcade$addRecipes(Collection<? extends RecipeHolder<?>> recipes) {
		List<RecipeHolder<?>> mutable = new LinkedList<>(recipes);
		mutable.addAll(this.byName.values());
		this.replaceRecipes(mutable);
	}

	@Override
	public void arcade$removeRecipes(Collection<? extends RecipeHolder<?>> recipes) {
		List<RecipeHolder<?>> mutable = new LinkedList<>(this.byName.values());
		mutable.removeAll(recipes);
		this.replaceRecipes(mutable);
	}
}
