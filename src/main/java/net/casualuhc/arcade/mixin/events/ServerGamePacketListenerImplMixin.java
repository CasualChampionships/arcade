package net.casualuhc.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.PlayerChatEvent;
import net.casualuhc.arcade.events.player.PlayerPackLoadEvent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@Inject(
		method = "handleResourcePackResponse",
		at = @At("TAIL")
	)
	private void onResourcePackStatus(ServerboundResourcePackPacket packet, CallbackInfo ci) {
		if (packet.getAction() == ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED) {
			PlayerPackLoadEvent event = new PlayerPackLoadEvent(this.player);
			EventHandler.broadcast(event);
		}
	}

	@WrapWithCondition(
		method = "broadcastChatMessage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"
		)
	)
	private boolean onBroadcastMessage(PlayerList instance, PlayerChatMessage message, ServerPlayer sender, ChatType.Bound boundChatType) {
		PlayerChatEvent event = new PlayerChatEvent(sender, message);
		EventHandler.broadcast(event);
		return !event.isCancelled();
	}
}
