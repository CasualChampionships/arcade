/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.gamemode;

import net.casual.arcade.minigame.gamemode.ExtendedGameMode;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(InsideBlockEffectType.class)
public class InsideBlockEffectTypeMixin {
    @Inject(
        method = "lambda$static$0",
        at = @At("TAIL")
    )
    private static void updateAdventureSpectatorFrozenTicks(Entity entity, CallbackInfo ci) {
        // The client doesn't know that it can't be frozen since it thinks it's in
        // adventure mode so the ticks frozen counter gets desynced
        if (entity instanceof ServerPlayer player && getExtendedGameMode(player) == ExtendedGameMode.AdventureSpectator) {
            // Mark the entity data dirty
            player.setTicksFrozen(1);
            player.clearFreeze();
        }
    }
}
