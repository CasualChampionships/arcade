/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.clock;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.dimensions.level.clock.DelegatedLevelClockManager;
import net.casual.arcade.dimensions.level.extensions.LevelClockExtension;
import net.casual.arcade.extensions.utils.ExtensionUtilsKt;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow
    public abstract ServerClockManager clockManager();

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$Builder;addDefaultLayers(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$Builder;"
        )
    )
    private void addClockExtension(CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        ExtensionUtilsKt.addExtension(self, new LevelClockExtension(self));
    }

    @ModifyExpressionValue(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"
        )
    )
    private ServerClockManager replaceWithLevelClockManager(ServerClockManager original) {
        return this.clockManager();
    }

    @ModifyReturnValue(
        method = "clockManager()Lnet/minecraft/world/clock/ServerClockManager;",
        at = @At("RETURN")
    )
    private ServerClockManager delegateClockManager(ServerClockManager original) {
        ServerLevel self = (ServerLevel) (Object) this;
        Optional<ResourceKey<WorldClock>> key = self.dimensionType().defaultClock().flatMap(Holder::unwrapKey);
        if (key.isPresent()) {
            LevelClockExtension extension = LevelClockExtension.getClockExtension(self);
            return new DelegatedLevelClockManager(key.get(), extension, original);
        }
        return original;
    }
}
