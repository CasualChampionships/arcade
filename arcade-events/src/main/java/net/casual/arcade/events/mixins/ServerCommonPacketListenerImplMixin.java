package net.casual.arcade.events.mixins;

import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.core.CancellableEvent;
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
	private Packet<?> onSendPacket(Packet<?> value) {
		ServerCommonPacketListenerImpl self = (ServerCommonPacketListenerImpl) (Object) this;
		CancellableEvent.Typed<Packet<?>> event = new ClientboundPacketEvent(this.server, this.playerProfile(), value);
		GlobalEventHandler.broadcast(event);

		if (self instanceof ServerGamePacketListenerImpl connection) {
			CancellableEvent.Typed<Packet<?>> old = event;
			event = new PlayerClientboundPacketEvent(connection.player, value);
			if (old.isCancelled()) {
				event.cancel(old.result());
			}
			GlobalEventHandler.broadcast(event);
		}
		return event.isCancelled() ? event.result() : value;
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
