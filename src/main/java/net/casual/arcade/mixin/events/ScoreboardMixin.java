package net.casual.arcade.mixin.events;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerTeamJoinEvent;
import net.casual.arcade.events.player.PlayerTeamLeaveEvent;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
	@Inject(
		method = "addPlayerToTeam",
		at = @At(value = "RETURN")
	)
	private void onPlayerJoinTeam(String username, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = PlayerUtils.player(username);
		if (player != null) {
			PlayerTeamJoinEvent event = new PlayerTeamJoinEvent(player, team);
			GlobalEventHandler.broadcast(event);
		}
	}

	@Inject(
		method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
		at = @At(
			value = "INVOKE",
			target = "Lit/unimi/dsi/fastutil/objects/Object2ObjectMap;remove(Ljava/lang/Object;)Ljava/lang/Object;",
			remap = false
		)
	)
	private void onPlayerLeaveTeam(String username, PlayerTeam team, CallbackInfo ci) {
		ServerPlayer player = PlayerUtils.player(username);
		if (player != null) {
			PlayerTeamLeaveEvent event = new PlayerTeamLeaveEvent(player, team);
			GlobalEventHandler.broadcast(event);
		}
	}
}
