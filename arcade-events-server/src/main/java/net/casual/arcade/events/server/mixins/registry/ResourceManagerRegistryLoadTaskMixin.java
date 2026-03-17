/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.registry;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.registry.RegistryLoadedFromResourcesEvent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ResourceManagerRegistryLoadTask.class)
public class ResourceManagerRegistryLoadTaskMixin<T> {
    // Not ideal, but we can't use scoped values since it's accessed from a different thread...
    @Unique private RegistryOps.RegistryInfoLookup context;

    @Inject(
        method = "load",
        at = @At("HEAD")
    )
    private void storeRegistryInfoLookup(RegistryOps.RegistryInfoLookup context, Executor executor, CallbackInfoReturnable<CompletableFuture<?>> cir) {
        this.context = context;
    }

    @Inject(
        method = "lambda$load$3",
        at = @At("HEAD")
    )
    private void onLoadRegistry(CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        ResourceManagerRegistryLoadTask<T> self = (ResourceManagerRegistryLoadTask<T>) (Object) this;
        RegistryLoadedFromResourcesEvent<T> event = new RegistryLoadedFromResourcesEvent<>(self, this.context);
        GlobalEventHandler.Server.broadcast(event);
    }
}
