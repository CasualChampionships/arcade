package net.casual.arcade.mixin.commands;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.StringReader;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerCommandEvent;
import net.casual.arcade.events.player.PlayerCommandSuggestionsEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@Shadow @Final private Connection connection;

	@Inject(
		method = "performChatCommand",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onChatCommand(ServerboundChatCommandPacket packet, LastSeenMessages lastSeenMessages, CallbackInfo ci) {
		PlayerCommandEvent event = new PlayerCommandEvent(this.player, packet.command());
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "handleCustomCommandSuggestions",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet, CallbackInfo ci) {
		String command = packet.getCommand().startsWith("/") ? packet.getCommand().substring(1) : packet.getCommand();
		PlayerCommandSuggestionsEvent event = new PlayerCommandSuggestionsEvent(this.player, command);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			event.result().thenAccept(suggestions -> {
				this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), suggestions));
			});
			ci.cancel();
		}
	}
}
