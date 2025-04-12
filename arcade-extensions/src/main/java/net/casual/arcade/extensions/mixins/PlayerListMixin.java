/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.extensions.Extension;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.TransferableEntityExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(
        method = "respawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V"
        )
    )
    private void onRestoreFrom(
        ServerPlayer player,
        boolean keepInventory,
        Entity.RemovalReason reason,
        CallbackInfoReturnable<ServerPlayer> cir,
        @Local(ordinal = 1) ServerPlayer respawned
    ) {
        boolean respawn = reason == Entity.RemovalReason.KILLED;
        List<Extension> transferred = new ArrayList<>();
        for (Extension extension : ExtensionHolder.all((ExtensionHolder) player)) {
            if (extension instanceof TransferableEntityExtension transferable) {
                transferred.add(transferable.transfer(respawned, respawn));
            }
        }
        for (Extension extension : transferred) {
            ExtensionHolder.add((ExtensionHolder) respawned, extension);
        }
    }
}
