package net.casual.arcade.mixin.screen;

import net.casual.arcade.gui.screen.SpectatorUsableScreen;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@Redirect(
		method = "handleContainerClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"
		)
	)
	private boolean canClick(ServerPlayer instance) {
		return !(instance.containerMenu instanceof SpectatorUsableScreen screen && screen.isSpectatorUsable()) && instance.isSpectator();
	}
}
