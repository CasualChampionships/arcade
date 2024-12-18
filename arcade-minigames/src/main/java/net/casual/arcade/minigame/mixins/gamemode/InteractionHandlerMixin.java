/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.gamemode;

import net.casual.arcade.minigame.gamemode.ExtendedGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class InteractionHandlerMixin {
	@Shadow @Final ServerGamePacketListenerImpl field_28963;

	@Inject(
		method = "onAttack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"
		),
		cancellable = true
	)
	private void onAttackInvalid(CallbackInfo ci) {
		if (getExtendedGameMode(this.field_28963.player) == ExtendedGameMode.AdventureSpectator) {
			// Vanilla client is silly and will sometimes do this
			ci.cancel();
		}
	}
}
