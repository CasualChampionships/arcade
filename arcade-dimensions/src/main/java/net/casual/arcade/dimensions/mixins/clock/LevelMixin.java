/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.clock;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Shadow
    public abstract RegistryAccess registryAccess();

    @Shadow
    public abstract @Nullable MinecraftServer getServer();

    @Inject(
        method = "getOverworldClockTime",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getTrueOverworldClockTime(CallbackInfoReturnable<Long> cir) {
        MinecraftServer server = this.getServer();
        if (server != null) {
            Long time = this.registryAccess().get(WorldClocks.OVERWORLD).map(holder -> {
                return server.clockManager().getTotalTicks(holder);
            }).orElse(0L);
            cir.setReturnValue(time);
        }
    }
}
