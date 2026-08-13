/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.gametest.utils.TestFakePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapWithCondition(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;[Ljava/lang/Object;)V"
        )
    )
    private boolean suppressTestPlayerJoinLog(
        Logger instance,
        String s,
        Object[] objects,
        @Local(argsOnly = true, name = "player") ServerPlayer player
    ) {
        return !(player instanceof TestFakePlayer);
    }

    @WrapWithCondition(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    private boolean suppressTestPlayerJoinMessage(
        PlayerList instance,
        Component message,
        boolean overlay,
        @Local(argsOnly = true, name = "player") ServerPlayer player
    ) {
        return !(player instanceof TestFakePlayer);
    }

    @Inject(
        method = "save",
        at = @At("HEAD"),
        cancellable = true
    )
    private void dontSaveTestPlayerData(ServerPlayer player, CallbackInfo ci) {
        if (player instanceof TestFakePlayer) {
            ci.cancel();
        }
    }
}
