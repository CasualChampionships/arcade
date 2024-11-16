package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerChatEvent;
import net.casual.arcade.events.player.PlayerJoinEvent;
import net.casual.arcade.events.player.PlayerRequestLoginEvent;
import net.casual.arcade.events.player.PlayerSystemMessageEvent;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "placeNewPlayer",
		at = @At("TAIL"),
		cancellable = true
	)
	private void onPlayerJoinPre(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		PlayerJoinEvent event = new PlayerJoinEvent(player);
		GlobalEventHandler.broadcast(event, Set.of(PlayerJoinEvent.PHASE_PRE));
		if (event.isCancelled()) {
			player.connection.disconnect(event.result());
			ci.cancel();
		}
	}

	@Inject(
		method = "placeNewPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onPlayerJoinInitialized(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		PlayerJoinEvent event = new PlayerJoinEvent(player);
		GlobalEventHandler.broadcast(event, Set.of(PlayerJoinEvent.PHASE_INITIALIZED));
		if (event.isCancelled()) {
			player.connection.disconnect(event.result());
			ci.cancel();
		}
	}

	@Inject(
		method = "placeNewPlayer",
		at = @At("TAIL"),
		cancellable = true
	)
	private void onPlayerJoinPost(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		PlayerJoinEvent event = new PlayerJoinEvent(player);
		GlobalEventHandler.broadcast(event, Set.of(BuiltInEventPhases.DEFAULT, PlayerJoinEvent.PHASE_POST));
		if (event.isCancelled()) {
			player.connection.disconnect(event.result());
			ci.cancel();
		}
	}

	@Inject(
		method = "canPlayerLogin",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onPlayerCanLogin(
		SocketAddress socketAddress,
		GameProfile gameProfile,
		CallbackInfoReturnable<Component> cir
	) {
		PlayerRequestLoginEvent event = new PlayerRequestLoginEvent(this.server, gameProfile, socketAddress);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(event.result());
		}
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
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
			return;
		}

		Predicate<ServerPlayer> filter = event.getFilter();
		Component replacement = event.getReplacementMessage();
		if (filter != null || replacement != null) {
			filter = filter == null ? (player) -> true : filter;
			replacement = replacement == null ? message.decoratedContent() : replacement;
			Component decorated;
			Component prefix = event.getMessagePrefix();
			if (prefix == null) {
				decorated = bound.chatType().value().chat().decorate(replacement, bound);
				prefix = Component.empty();
			} else {
				decorated = replacement;
			}
			PlayerUtils.broadcastMessageAsSystem(
				sender,
				decorated,
				filter,
				prefix
			);
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
		GlobalEventHandler.broadcast(event);
		component = event.getMessage();
		if (!event.isCancelled()) {
			original.call(instance, component, bypassHiddenChat);
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
		@Local(argsOnly = true) ServerPlayer player
	) {
		PlayerSystemMessageEvent.broadcast(player, instance, message, bypassHiddenChat, original);
	}
}
