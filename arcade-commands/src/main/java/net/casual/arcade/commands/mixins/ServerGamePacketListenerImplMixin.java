package net.casual.arcade.commands.mixins;

import net.casual.arcade.commands.hidden.HiddenCommandManager;
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
		method = "performUnsignedChatCommand",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onCommand(String command, CallbackInfo ci) {
		if (HiddenCommandManager.onCommand(this.player, command)) {
			ci.cancel();
		}
	}
}
