package net.casual.arcade.mixin.commands;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerCommandEvent;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

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
}
