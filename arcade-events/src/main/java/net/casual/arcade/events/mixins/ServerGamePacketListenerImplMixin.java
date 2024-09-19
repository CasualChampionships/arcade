package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.suggestion.Suggestions;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.*;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.PlayerChatMessage;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
	@Shadow public ServerPlayer player;

	public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
		super(server, connection, cookie);
	}

	@WrapWithCondition(
		method = "broadcastChatMessage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"
		)
	)
	private boolean onBroadcastMessage(PlayerList instance, PlayerChatMessage message, ServerPlayer sender, ChatType.Bound bound) {
		PlayerChatEvent event = new PlayerChatEvent(sender, message);
		GlobalEventHandler.broadcast(event);
		boolean notCancelled = !event.isCancelled();
		if (notCancelled) {
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
				return false;
			}
		}
		return notCancelled;
	}

	@Inject(
		method = "onDisconnect",
		at = @At("HEAD")
	)
	private void onDisconnect(CallbackInfo ci) {
		PlayerLeaveEvent event = new PlayerLeaveEvent(this.player);
		GlobalEventHandler.broadcast(event);
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
	private void onRespawn(ServerboundClientCommandPacket packet, CallbackInfo ci) {
		PlayerRespawnEvent event = new PlayerRespawnEvent(this.player);
		GlobalEventHandler.broadcast(event);
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
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		if (event.isCancelled()) {
			return;
		}
		original.call(instance, slotId, button, action, player);
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES);
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
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "performSignedChatCommand",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onChatCommand(ServerboundChatCommandSignedPacket packet, LastSeenMessages lastSeenMessages, CallbackInfo ci) {
		PlayerCommandEvent event = new PlayerCommandEvent(this.player, packet.command());
		GlobalEventHandler.broadcast(event);
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
		GlobalEventHandler.broadcast(event);
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
		GlobalEventHandler.broadcast(event);

		List<CompletableFuture<Suggestions>> all = event.getAllSuggestions();
		CompletableFuture<Void> futures = CompletableFuture.allOf(all.toArray(CompletableFuture[]::new));

		CompletableFuture<List<Suggestions>> collected = futures.thenApply(v -> all.stream().map(CompletableFuture::join).toList());

		return collected.thenAccept(suggestions -> {
			Suggestions merged = Suggestions.merge(packet.getCommand(), suggestions);
			this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), merged));
		});
	}
}
