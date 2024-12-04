package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.network.ClientboundPacketEvent;
import net.casual.arcade.events.network.PlayerDisconnectEvent;
import net.casual.arcade.events.player.PlayerClientboundPacketEvent;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
	@Shadow @Final protected MinecraftServer server;

	@Shadow protected abstract GameProfile playerProfile();

	@ModifyVariable(
		method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
		at = @At("HEAD"),
		argsOnly = true
	)
	private Packet<?> onSendPacket(Packet<?> value, @Cancellable CallbackInfo ci) {
		ServerCommonPacketListenerImpl self = (ServerCommonPacketListenerImpl) (Object) this;
		ClientboundPacketEvent event = new ClientboundPacketEvent(this.server, this.playerProfile(), value);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
			return event.getPacket();
		}

		if (self instanceof ServerGamePacketListenerImpl connection) {
			PlayerClientboundPacketEvent playerEvent = new PlayerClientboundPacketEvent(connection.player, event.getPacket());
			GlobalEventHandler.broadcast(playerEvent);
			if (event.isCancelled()) {
				ci.cancel();
			}
			return playerEvent.getPacket();
		}
		return event.getPacket();
	}

	@Inject(
		method = "onDisconnect",
		at = @At("HEAD")
	)
	private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
		PlayerDisconnectEvent event = new PlayerDisconnectEvent(this.server, this.playerProfile());
		GlobalEventHandler.broadcast(event);
	}
}
