package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.common.registry.RegistryEventHandler;
import net.casual.arcade.events.common.registry.RegistryLoadedFromResourcesEvent;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
		GlobalEventHandler.Server.broadcast(event);
	}

	@Inject(
		method = "loadContentsFromManager",
		at = @At("HEAD")
	)
	private static void preLoadRegistries(CallbackInfo ci) {
		RegistryEventHandler.load();
	}

	@Inject(
		method = "loadContentsFromManager",
		at = @At("RETURN")
	)
	private static void postLoadRegistries(CallbackInfo ci) {
		RegistryEventHandler.unload();
	}
}
