/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.level;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.dimensions.level.CustomLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerWaypointManager.class)
public class ServerWaypointManagerMixin {
    @ModifyReceiver(
        method = "isLocatorBarEnabledFor",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"
        )
    )
    private static GameRules fixVanillaLocatorBarGameRuleCheck(
        GameRules original,
        GameRules.Key<GameRules.BooleanValue> key,
        @Local(argsOnly = true) ServerPlayer player
    ) {
        if (player.level() instanceof CustomLevel) {
            return player.level().getGameRules();
        }
        return original;
    }
}
