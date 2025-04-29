/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.suggestion.Suggestions;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.*;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
	@Unique private static final ThreadLocal<PlayerLeaveEvent> PLAYER_LEAVE_CONTEXT = new ThreadLocal<>();

	@Shadow public ServerPlayer player;

	public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
		super(server, connection, cookie);
	}

	@Inject(
		method = "onDisconnect",
		at = @At("HEAD")
	)
	private void onDisconnectPre(CallbackInfo ci) {
		PlayerLeaveEvent event = new PlayerLeaveEvent(this.player);
		PLAYER_LEAVE_CONTEXT.set(event);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
	}

	@WrapWithCondition(
		method = "removePlayerFromWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private boolean onBroadcastLeaveMessage(PlayerList instance, Component message, boolean bypassHiddenChat) {
		PlayerLeaveEvent event = PLAYER_LEAVE_CONTEXT.get();
        return event == null || event.getLeaveMessageModification() != PlayerLeaveEvent.LeaveMessageModification.Hide;
    }

	@Inject(
		method = "onDisconnect",
		at = @At("RETURN")
	)
	private void onDisconnectPost(CallbackInfo ci) {
		PlayerLeaveEvent event = PLAYER_LEAVE_CONTEXT.get();
		if (event == null) {
			event = new PlayerLeaveEvent(this.player);
		}
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
        PLAYER_LEAVE_CONTEXT.remove();
	}

	@Inject(
		method = "handleClientCommand",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/server/players/PlayerList;respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	@SuppressWarnings("DiscouragedShift")
	private void onRespawn(ServerboundClientCommandPacket packet, CallbackInfo ci) {
		PlayerRespawnEvent event = new PlayerRespawnEvent(this.player);
		GlobalEventHandler.Server.broadcast(event);
	}

	@WrapOperation(
		method = "removePlayerFromWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private void onBroadcastLeaveMessage(
		PlayerList instance,
		Component message,
		boolean bypassHiddenChat,
		Operation<Void> original
	) {
		PlayerSystemMessageEvent.broadcast(this.player, instance, message, bypassHiddenChat, original);
	}

	@WrapOperation(
		method = "handleContainerClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V"
		)
	)
	private void onSlotClicked(
		AbstractContainerMenu instance,
		int slotId,
		int button,
		ClickType action,
		Player player,
		Operation<Void> original
	) {
		PlayerSlotClickEvent event = new PlayerSlotClickEvent(this.player, instance, slotId, button, action);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		if (event.isCancelled()) {
			return;
		}
		original.call(instance, slotId, button, action, player);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}

	@Inject(
		method = "handlePlayerCommand",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;setShiftKeyDown(Z)V"
		)
	)
	private void onSetSneaking(ServerboundPlayerCommandPacket packet, CallbackInfo ci) {
		PlayerSetSneakingEvent event = new PlayerSetSneakingEvent(this.player, packet.getAction() == PRESS_SHIFT_KEY);
		GlobalEventHandler.Server.broadcast(event);
	}

	@Inject(
		method = "performSignedChatCommand",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onChatCommand(ServerboundChatCommandSignedPacket packet, LastSeenMessages lastSeenMessages, CallbackInfo ci) {
		PlayerCommandEvent event = new PlayerCommandEvent(this.player, packet.command());
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "performUnsignedChatCommand",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onChatCommand(String command, CallbackInfo ci) {
		PlayerCommandEvent event = new PlayerCommandEvent(this.player, command);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Redirect(
		method = "handleCustomCommandSuggestions",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;",
			remap = false
		)
	)
	private CompletableFuture<?> onCustomCommandSuggestions(
		CompletableFuture<Suggestions> vanillaSuggestions,
		Consumer<? super Suggestions> action,
		ServerboundCommandSuggestionPacket packet
	) {
		PlayerCommandSuggestionsEvent event = new PlayerCommandSuggestionsEvent(this.player, packet.getCommand());
		event.addSuggestions(vanillaSuggestions);
		GlobalEventHandler.Server.broadcast(event);

		List<CompletableFuture<Suggestions>> all = event.getAllSuggestions();
		return Util.sequenceFailFast(all).thenAccept(suggestions -> {
			Suggestions merged = Suggestions.merge(packet.getCommand(), suggestions);
			this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), merged));
		});
	}

	@Inject(
		method = "handleTeleportToEntityPacket",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onSpectatorTeleport(ServerboundTeleportToEntityPacket packet, CallbackInfo ci) {
		UUID target = ((ServerboundTeleportToEntityPacketAccessor) packet).getUUID();
		PlayerSpectatorTeleportEvent event = new PlayerSpectatorTeleportEvent(this.player, target);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}
}
