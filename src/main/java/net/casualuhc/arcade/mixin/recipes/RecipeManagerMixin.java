package net.casualuhc.arcade.mixin.recipes;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.casualuhc.arcade.events.GlobalEventHandler;
import net.casualuhc.arcade.events.server.ServerRecipeReloadEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
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
		@Local(ordinal = 1) Map<RecipeType<?>, ImmutableMap.Builder<ResourceLocation, Recipe<?>>> recipesByType,
		@Local ImmutableMap.Builder<ResourceLocation, Recipe<?>> recipesByName
	) {
		ServerRecipeReloadEvent event = new ServerRecipeReloadEvent((RecipeManager) (Object) this, resourceManager);
		GlobalEventHandler.broadcast(event);

		for (Recipe<?> recipe : event.getRecipes()) {
			recipesByType.computeIfAbsent(recipe.getType(), recipeType -> ImmutableMap.builder()).put(recipe.getId(), recipe);
			recipesByName.put(recipe.getId(), recipe);
		}
	}
}
