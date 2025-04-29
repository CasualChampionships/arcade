/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.PlayerChatEvent;
import net.casual.arcade.events.server.player.PlayerJoinEvent;
import net.casual.arcade.events.server.player.PlayerJoinEvent.JoinMessageModification;
import net.casual.arcade.events.server.player.PlayerRequestLoginEvent;
import net.casual.arcade.events.server.player.PlayerSystemMessageEvent;
import net.casual.arcade.utils.PlayerUtils;
import net.casual.arcade.utils.chat.PlayerFormattedChat;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.SocketAddress;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "placeNewPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onPlayerJoinInitialized(
		CallbackInfo ci,
		@Local(argsOnly = true) ServerPlayer player,
		@Share("event") LocalRef<PlayerJoinEvent> eventRef
	) {
		PlayerJoinEvent event = new PlayerJoinEvent(player);
		GlobalEventHandler.Server.broadcast(event, Set.of(PlayerJoinEvent.PHASE_INITIALIZED));
		if (event.isCancelled()) {
			player.connection.disconnect(event.result());
			ci.cancel();
		}
		eventRef.set(event);
	}

	@Inject(
		method = "placeNewPlayer",
		at = @At("TAIL"),
		cancellable = true
	)
	private void onPlayerJoinPost(
		CallbackInfo ci,
		@Share("event") LocalRef<PlayerJoinEvent> eventRef,
		@Share("delayed") LocalRef<Runnable> delayedRef
	) {
		PlayerJoinEvent event = eventRef.get();
		GlobalEventHandler.Server.broadcast(event, Set.of(BuiltInEventPhases.DEFAULT, PlayerJoinEvent.PHASE_POST));
		if (event.isCancelled()) {
			event.getPlayer().connection.disconnect(event.result());
			ci.cancel();
		}

		Runnable runnable = delayedRef.get();
		if (runnable != null) {
			runnable.run();
		}
	}

	@WrapOperation(
		method = "placeNewPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private void onBroadcastJoinGame(
		PlayerList instance,
		Component message,
		boolean bypassHiddenChat,
		Operation<Void> original,
		@Local(argsOnly = true) ServerPlayer player,
		@Share("event") LocalRef<PlayerJoinEvent> eventRef,
		@Share("delayed") LocalRef<Runnable> delayedRef
	) {
		PlayerJoinEvent event = eventRef.get();
		if (event.getJoinMessageModification() == JoinMessageModification.Hide) {
			return;
		}
		if (event.getJoinMessageModification() == JoinMessageModification.Delay || event.getDelayJoinMessage()) {
			delayedRef.set(() -> PlayerSystemMessageEvent.broadcast(player, instance, message, bypassHiddenChat, original));
		} else {
			PlayerSystemMessageEvent.broadcast(player, instance, message, bypassHiddenChat, original);
		}
	}

	@ModifyReturnValue(
		method = "canPlayerLogin",
		at = @At("RETURN")
	)
	private Component onPlayerCanLogin(
		Component original,
		SocketAddress socketAddress,
		GameProfile gameProfile
	) {
		PlayerRequestLoginEvent event = new PlayerRequestLoginEvent(this.server, gameProfile, socketAddress);
		if (original != null) {
			event.deny(original);
		}
		GlobalEventHandler.Server.broadcast(event);
		return event.getReason();
	}

	@Inject(
		method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onBroadcastChatMessage(
		PlayerChatMessage message,
		Predicate<ServerPlayer> shouldFilterMessageTo,
		@Nullable ServerPlayer sender,
		ChatType.Bound bound,
		CallbackInfo ci
	) {
		if (sender == null) {
			return;
		}

		PlayerChatEvent event = new PlayerChatEvent(sender, message);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
			return;
		}

		Predicate<ServerPlayer> filter = event.getFilter();
		if (filter != null || event.hasMutated()) {
			PlayerFormattedChat formatted = event.formatted();
			filter = filter == null ? player -> true : filter;
			Component prefix = formatted.getPrefix();
			Component username = formatted.getUsername();
			Component replacement = formatted.getMessage();
			Component decorated;
			if (username == null) {
				// Format the username using the vanilla decorator
				decorated = bound.chatType().value().chat().decorate(replacement, bound);
				username = CommonComponents.EMPTY;
			} else {
				decorated = replacement;
			}
			PlayerUtils.broadcastMessageAsSystem(sender, decorated, filter, username, prefix);
			ci.cancel();
		}
	}

	@WrapOperation(
		method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Z)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private void onSendSystemMessage(
		ServerPlayer instance,
		Component component,
		boolean bypassHiddenChat,
		Operation<Void> original
	) {
		PlayerSystemMessageEvent event = new PlayerSystemMessageEvent(instance, component, bypassHiddenChat);
		GlobalEventHandler.Server.broadcast(event);
		component = event.getMessage();
		if (!event.isCancelled()) {
			original.call(instance, component, bypassHiddenChat);
		}
	}
}
