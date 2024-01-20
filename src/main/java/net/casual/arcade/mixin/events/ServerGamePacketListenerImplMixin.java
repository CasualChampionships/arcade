package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerChatEvent;
import net.casual.arcade.events.player.PlayerLeaveEvent;
import net.casual.arcade.events.player.PlayerRespawnEvent;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
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
					decorated = bound.chatType().chat().decorate(replacement, bound);
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
			target = "Lnet/minecraft/server/players/PlayerList;respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	private void onRespawn(ServerboundClientCommandPacket packet, CallbackInfo ci) {
		PlayerRespawnEvent event = new PlayerRespawnEvent(this.player);
		GlobalEventHandler.broadcast(event);
	}
}
