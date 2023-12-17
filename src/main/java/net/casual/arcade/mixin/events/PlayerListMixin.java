package net.casual.arcade.mixin.events;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerJoinEvent;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(
		method = "placeNewPlayer",
		at = @At("TAIL")
	)
	private void onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		PlayerJoinEvent event = new PlayerJoinEvent(player);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			connection.disconnect(event.result());
		}
	}
}
