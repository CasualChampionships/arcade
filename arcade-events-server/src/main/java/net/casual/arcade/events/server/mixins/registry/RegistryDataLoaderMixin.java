/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.registry;

import net.casual.arcade.events.server.registry.RegistryEventHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {
	@Inject(
		method = "lambda$load$0",
		at = @At("HEAD")
	)
	private static void preLoadRegistries(CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
		RegistryEventHandler.load();
	}

	@Inject(
		method = "lambda$load$2",
		at = @At("RETURN")
	)
	private static void postLoadRegistries(CallbackInfoReturnable<RegistryAccess.Frozen> cir) {
		RegistryEventHandler.unload();
	}
}
