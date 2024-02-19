package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.core.CancellableEvent;
import net.casual.arcade.events.network.ClientboundPacketEvent;
import net.casual.arcade.events.network.PackStatusEvent;
import net.casual.arcade.events.network.PlayerDisconnectEvent;
import net.casual.arcade.events.player.PlayerClientboundPacketEvent;
import net.casual.arcade.resources.PackStatus;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
	@Shadow @Final protected MinecraftServer server;

	@Shadow protected abstract GameProfile playerProfile();

	@Inject(
		method = "handleResourcePackResponse",
		at = @At("TAIL")
	)
	private void onResourcePackResponse(ServerboundResourcePackPacket packet, CallbackInfo ci) {
		PackStatusEvent event = new PackStatusEvent(
			this.server,
			this.playerProfile(),
			packet.id(),
			PackStatus.toPackStatus(packet.action())
		);
		GlobalEventHandler.broadcast(event);
	}

	@WrapWithCondition(
		method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V"
		)
	)
	private boolean onSendPacket(Connection instance, Packet<?> packet, @Nullable PacketSendListener listener, boolean flush) {
		ServerCommonPacketListenerImpl self = (ServerCommonPacketListenerImpl) (Object) this;
		CancellableEvent.Default event = new ClientboundPacketEvent(this.server, this.playerProfile(), packet);
		GlobalEventHandler.broadcast(event);

		if (self instanceof ServerGamePacketListenerImpl connection) {
			CancellableEvent.Default old = event;
			event = new PlayerClientboundPacketEvent(connection.player, packet);
			if (old.isCancelled()) {
				event.cancel();
			}
			GlobalEventHandler.broadcast(event);
		}

		return !event.isCancelled();
	}

	@Inject(
		method = "onDisconnect",
		at = @At("HEAD")
	)
	private void onDisconnect(Component reason, CallbackInfo ci) {
		PlayerDisconnectEvent event = new PlayerDisconnectEvent(this.server, this.playerProfile());
		GlobalEventHandler.broadcast(event);
	}
}
