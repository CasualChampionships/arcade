package net.casualuhc.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casualuhc.arcade.events.GlobalEventHandler;
import net.casualuhc.arcade.events.player.PlayerChatEvent;
import net.casualuhc.arcade.events.player.PlayerClientboundPacketEvent;
import net.casualuhc.arcade.events.player.PlayerLeaveEvent;
import net.casualuhc.arcade.events.player.PlayerPackLoadEvent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

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
			GlobalEventHandler.broadcast(event);
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
		GlobalEventHandler.broadcast(event);
		return !event.isCancelled();
	}

	@Inject(
		method = "disconnect",
		at = @At("HEAD")
	)
	private void onDisconnect(CallbackInfo ci) {
		PlayerLeaveEvent event = new PlayerLeaveEvent(this.player);
		GlobalEventHandler.broadcast(event);
	}

	@ModifyVariable(
		method = "send(Lnet/minecraft/network/protocol/Packet;)V",
		at = @At("HEAD"),
		argsOnly = true
	)
	private Packet<?> onSendPacket(Packet<?> packet) {
		PlayerClientboundPacketEvent event = new PlayerClientboundPacketEvent(this.player, packet);
		if (event.isCancelled()) {
			return event.result();
		}
		return packet;
	}
}
