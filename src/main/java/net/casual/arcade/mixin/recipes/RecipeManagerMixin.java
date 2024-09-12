package net.casual.arcade.mixin.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.ducks.MutableRecipeManager;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ServerRecipeReloadEvent;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.casual.arcade.utils.impl.ConcatenatedList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements MutableRecipeManager {
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
		@Local ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> recipesByType,
		@Local ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> recipesByName
	) {
		ServerRecipeReloadEvent event = new ServerRecipeReloadEvent((RecipeManager) (Object) this, resourceManager);
		GlobalEventHandler.broadcast(event);

		for (RecipeHolder<?> recipe : event.getRecipes()) {
			recipesByType.put(recipe.value().getType(), recipe);
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

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@ModifyReturnValue(
		method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/crafting/RecipeHolder;)Ljava/util/Optional;",
		at = @At("RETURN")
	)
	private <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> modifyRecipe(
		Optional<RecipeHolder<T>> original,
		RecipeType<T> type,
		I input,
		Level level
	) {
		if (original.isPresent() || !(level instanceof ServerLevel serverLevel)) {
			return original;
		}
		Minigame<?> minigame = MinigameUtils.getMinigame(serverLevel);
		if (minigame == null) {
			return original;
		}

		return minigame.getRecipes().find(type, input, serverLevel);
 	}


	@ModifyReturnValue(
		method = "getRecipesFor",
		at = @At("RETURN")
	)
	private <I extends RecipeInput, T extends Recipe<I>> List<RecipeHolder<T>> modifyRecipes(
		List<RecipeHolder<T>> original,
		RecipeType<T> type,
		I input,
		Level level
	) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return original;
		}
		Minigame<?> minigame = MinigameUtils.getMinigame(serverLevel);
		if (minigame == null) {
			return original;
		}
		return ConcatenatedList.concat(original, minigame.getRecipes().findAll(type, input, serverLevel));
	}
}
