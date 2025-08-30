/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.player;

import com.mojang.authlib.GameProfile;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin {
    @Shadow
    @Final
    private GameProfile gameProfile;

    @Inject(
        method = "handleConfigurationFinished",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V",
            shift = At.Shift.BEFORE
        )
    )
    @SuppressWarnings("DiscouragedShift")
    private void beforePlacePlayer(
        ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket,
        CallbackInfo ci
    ) {
        Collection<ReplayPlayerRecorder> recorder = ReplayPlayerRecorders.get(this.gameProfile.getId());
        recorder.forEach(ReplayPlayerRecorder::afterConfigure);
    }
}
