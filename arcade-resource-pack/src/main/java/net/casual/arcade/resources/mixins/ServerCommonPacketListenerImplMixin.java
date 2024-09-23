package net.casual.arcade.resources.mixins;

import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.resources.pack.PackStatus;
import net.casual.arcade.resources.event.PackStatusEvent;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
	@Shadow
	@Final
	protected MinecraftServer server;

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
}
