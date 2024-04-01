package net.casual.arcade.mixin.events;

import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerRequestLoginEvent;
import net.casual.arcade.events.player.PlayerJoinEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "placeNewPlayer",
		at = @At("TAIL")
	)
	private void onPlayerJoin(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		PlayerJoinEvent event = new PlayerJoinEvent(player);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			player.connection.disconnect(event.result());
		}
	}

	@Inject(
		method = "canPlayerLogin",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onPlayerCanLogin(
		SocketAddress socketAddress,
		GameProfile gameProfile,
		CallbackInfoReturnable<Component> cir
	) {
		PlayerRequestLoginEvent event = new PlayerRequestLoginEvent(this.server, gameProfile, socketAddress);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(event.result());
		}
	}
}
