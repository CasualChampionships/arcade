package net.casual.arcade.mixin.commands;

import com.mojang.brigadier.suggestion.Suggestions;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
		String command = packet.getCommand().startsWith("/") ? packet.getCommand().substring(1) : packet.getCommand();
		PlayerCommandSuggestionsEvent event = new PlayerCommandSuggestionsEvent(this.player, command);
		event.addSuggestions(vanillaSuggestions);
		GlobalEventHandler.broadcast(event);

		List<CompletableFuture<Suggestions>> all = event.getAllSuggestions();
		CompletableFuture<Void> futures = CompletableFuture.allOf(all.toArray(CompletableFuture[]::new));

		CompletableFuture<List<Suggestions>> collected = futures.thenApply(v -> all.stream().map(CompletableFuture::join).toList());

		return collected.thenAccept(suggestions -> {
			Suggestions merged = Suggestions.merge(command, suggestions);
			this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), merged));
		});
	}
}
