/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent;
import net.casual.arcade.events.server.player.PlayerTeamLeaveEvent;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerScoreboard.class)
public class ScoreboardMixin {
	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "addPlayerToTeam",
		at = @At("HEAD")
	)
	private void onPlayerJoinTeam(String username, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = PlayerUtils.player(this.server, username);
		if (player != null) {
			PlayerTeamJoinEvent event = new PlayerTeamJoinEvent(player, team);
			GlobalEventHandler.Server.broadcast(event);
		}
	}

	@Inject(
		method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
		at = @At("HEAD")
	)
	private void onPlayerLeaveTeam(String username, PlayerTeam team, CallbackInfo ci) {
		ServerPlayer player = PlayerUtils.player(this.server, username);
		if (player != null) {
			PlayerTeamLeaveEvent event = new PlayerTeamLeaveEvent(player, team);
			GlobalEventHandler.Server.broadcast(event);
		}
	}
}
