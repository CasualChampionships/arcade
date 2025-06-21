/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.network.ClientboundPacketEvent;
import net.casual.arcade.events.server.player.PlayerDisconnectEvent;
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
	@Shadow @Final protected MinecraftServer server;

	@Shadow protected abstract GameProfile playerProfile();

	@ModifyVariable(
		method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
		at = @At("HEAD"),
		argsOnly = true
	)
	private Packet<?> onSendPacket(Packet<?> value, @Cancellable CallbackInfo ci) {
		ServerCommonPacketListenerImpl self = (ServerCommonPacketListenerImpl) (Object) this;
		ClientboundPacketEvent event = new ClientboundPacketEvent(this.server, this.playerProfile(), value);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		if (event.isCancelled()) {
			ci.cancel();
			return event.getPacket();
		}

		if (self instanceof ServerGamePacketListenerImpl connection) {
			PlayerClientboundPacketEvent playerEvent = new PlayerClientboundPacketEvent(connection.player, event.getPacket());
			GlobalEventHandler.Server.broadcast(playerEvent, BuiltInEventPhases.PRE_PHASES);
			if (playerEvent.isCancelled()) {
				ci.cancel();
			}
			return playerEvent.getPacket();
		}
		return event.getPacket();
	}

	@WrapOperation(
		method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"
		)
	)
	private void onSendPacket(
		Connection instance,
		Packet<?> packet,
		@Nullable ChannelFutureListener listener,
		boolean flush,
		Operation<Void> original
	) {
		original.call(instance, packet, listener, flush);
		ClientboundPacketEvent event = new ClientboundPacketEvent(this.server, this.playerProfile(), packet);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);

		if ((Object) this instanceof ServerGamePacketListenerImpl connection) {
			PlayerClientboundPacketEvent playerEvent = new PlayerClientboundPacketEvent(connection.player, event.getPacket());
			GlobalEventHandler.Server.broadcast(playerEvent, BuiltInEventPhases.POST_PHASES);
		}
	}

	@Inject(
		method = "onDisconnect",
		at = @At("HEAD")
	)
	private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
		PlayerDisconnectEvent event = new PlayerDisconnectEvent(this.server, this.playerProfile());
		GlobalEventHandler.Server.broadcast(event);
	}
}
