/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.registry;

import net.casual.arcade.events.server.registry.RegistryEventHandler;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
    @Inject(
        method = "lambda$load$0",
        at = @At("HEAD")
    )
    private static void beforeDynamicRegistriesLoad(CallbackInfoReturnable<?> cir) {
        RegistryEventHandler.load();
    }

    @Inject(
        method = "lambda$load$3",
        at = @At("HEAD")
    )
    private static void afterDynamicRegistriesLoad(
        CloseableResourceManager resources,
        ReloadableServerResources managers,
        Throwable throwable,
        CallbackInfo ci
    ) {
        RegistryEventHandler.unload();
    }
}
