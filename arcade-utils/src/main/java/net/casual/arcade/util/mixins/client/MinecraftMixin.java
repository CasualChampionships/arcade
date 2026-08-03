/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.client;

import net.casual.arcade.utils.coroutine.ClientCoroutineUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(
        method = "tick",
        at = @At("HEAD")
    )
    private void startClientCoroutinesTick(CallbackInfo ci) {
        ClientCoroutineUtils.INSTANCE.onTickStart((Minecraft) (Object) this);
    }

    @Inject(
        method = "tick",
        at = @At("TAIL")
    )
    private void tickClientCoroutines(CallbackInfo ci) {
        ProfilerFiller filler = Profiler.get();
        filler.push("coroutine_tick_delayed_tasks");
        ClientCoroutineUtils.INSTANCE.onTick((Minecraft) (Object) this);
        filler.pop();
    }

    @Inject(
        method = "close",
        at = @At("HEAD")
    )
    private void shutdownClientCoroutines(CallbackInfo ci) {
        ClientCoroutineUtils.INSTANCE.onStop((Minecraft) (Object) this);
    }
}
