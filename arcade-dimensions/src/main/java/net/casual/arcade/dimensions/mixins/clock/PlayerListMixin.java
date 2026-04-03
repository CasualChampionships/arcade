/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.clock;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.dimensions.level.extensions.LevelClockExtension;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @ModifyExpressionValue(
        method = "sendLevelInfo",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/clock/ServerClockManager;createFullSyncPacket()Lnet/minecraft/network/protocol/game/ClientboundSetTimePacket;"
        )
    )
    private ClientboundSetTimePacket appendLevelClockTime(
        ClientboundSetTimePacket original,
        @Local(name = "level") ServerLevel level
    ) {
        return LevelClockExtension.modifyClientboundSetTimePacket(original, level);
    }
}
