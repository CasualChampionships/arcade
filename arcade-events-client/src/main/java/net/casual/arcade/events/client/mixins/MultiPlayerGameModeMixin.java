/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.player.PlayerTryAttackEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Inject(
        method = "attack",
        at = @At("HEAD"),
        cancellable = true
    )
    private void broadcastPlayerTryAttack(Player player, Entity entity, CallbackInfo ci) {
        PlayerTryAttackEvent event = new PlayerTryAttackEvent((LocalPlayer) player, entity);
        GlobalEventHandler.Client.broadcast(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
