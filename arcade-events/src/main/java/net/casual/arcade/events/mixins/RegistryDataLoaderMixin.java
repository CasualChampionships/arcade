package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.registry.RegistryEventHandler;
import net.casual.arcade.events.registry.RegistryLoadedFromResourcesEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
	@Inject(
		method = "loadContentsFromManager",
		at = @At("TAIL")
	)
	private static <E> void onLoadRegistry(
		CallbackInfo ci,
		@Local(argsOnly = true) WritableRegistry<E> registry
	) {
		RegistryLoadedFromResourcesEvent<E> event = new RegistryLoadedFromResourcesEvent<>(registry);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
		at = @At("HEAD")
	)
	private static void preLoadRegistries(CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
		RegistryEventHandler.load();
	}

	@Inject(
		method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/RegistryAccess;Ljava/util/List;)Lnet/minecraft/core/RegistryAccess$Frozen;",
		at = @At("RETURN")
	)
	private static void postLoadRegistries(CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
		RegistryEventHandler.unload();
	}
}
