/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.PlayerLoginEvent;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplMixin {
	@Shadow @Nullable private GameProfile authenticatedProfile;

	@Shadow @Final MinecraftServer server;

	@Inject(
		method = "handleLoginAcknowledgement",
		at = @At(
			value = "INVOKE",
			target = "Lorg/apache/commons/lang3/Validate;validState(ZLjava/lang/String;[Ljava/lang/Object;)V",
			shift = At.Shift.AFTER,
			remap = false
		)
	)
	private void onPlayerLogin(ServerboundLoginAcknowledgedPacket packet, CallbackInfo ci) {
		if (this.authenticatedProfile != null) {
			PlayerLoginEvent event = new PlayerLoginEvent(this.server, this.authenticatedProfile);
			GlobalEventHandler.Server.broadcast(event);
		}
	}
}
