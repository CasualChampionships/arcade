package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.*;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

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
		eu.pb4.sgui.api.ClickType type = eu.pb4.sgui.api.ClickType.toClickType(action, button, slotId);
		PlayerSlotClickedEvent event = new PlayerSlotClickedEvent(this.player, instance, slotId, type, action);
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		if (event.isCancelled()) {
			return;
		}
		original.call(instance, slotId, button, action, player);
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}
}
