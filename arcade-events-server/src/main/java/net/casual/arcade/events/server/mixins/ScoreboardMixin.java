/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.common.Event;
import net.casual.arcade.events.server.entity.EntityTeamJoinEvent;
import net.casual.arcade.events.server.entity.EntityTeamLeaveEvent;
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent;
import net.casual.arcade.events.server.player.PlayerTeamLeaveEvent;
import net.casual.arcade.utils.EntityUtilsKt;
import net.casual.arcade.utils.PlayerUtils;
import net.casual.arcade.utils.StringUtilsKt;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(ServerScoreboard.class)
public class ScoreboardMixin {
	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "addPlayerToTeam",
		at = @At("HEAD")
	)
	private void onPlayerJoinTeam(
		String username,
		PlayerTeam team,
		CallbackInfoReturnable<Boolean> cir,
		@Share("events") LocalRef<List<Event>> events
	) {
		events.set(new ArrayList<>(2));
		ServerPlayer player = PlayerUtils.player(this.server, username);
		Entity entity = player;
		if (player != null) {
			PlayerTeamJoinEvent event = new PlayerTeamJoinEvent(player, team);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			events.get().add(event);
		} else if (StringUtilsKt.isUUID(username)) {
			entity = EntityUtilsKt.findEntity(this.server, UUID.fromString(username));
		}
		if (entity != null) {
			EntityTeamJoinEvent event = new EntityTeamJoinEvent(entity, team);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			events.get().add(event);
		}
	}

	@Inject(
		method = "addPlayerToTeam",
		at = @At("RETURN")
	)
	private void onPlayerJoinTeamPost(
		CallbackInfoReturnable<Boolean> cir,
		@Share("events") LocalRef<List<Event>> events
	) {
		for (Event event : events.get()) {
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
		}
	}

	@Inject(
		method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
		at = @At("HEAD")
	)
	private void onPlayerLeaveTeam(
		String username,
		PlayerTeam team,
		CallbackInfo ci,
		@Share("events") LocalRef<List<Event>> events
	) {
		events.set(new ArrayList<>(2));
		ServerPlayer player = PlayerUtils.player(this.server, username);
		Entity entity = player;
		if (player != null) {
			PlayerTeamLeaveEvent event = new PlayerTeamLeaveEvent(player, team);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			events.get().add(event);
		} else if (StringUtilsKt.isUUID(username)) {
			entity = EntityUtilsKt.findEntity(this.server, UUID.fromString(username));
		}
		if (entity != null) {
			EntityTeamLeaveEvent event = new EntityTeamLeaveEvent(entity, team);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			events.get().add(event);
		}
	}

	@Inject(
		method = "removePlayerFromTeam",
		at = @At("TAIL")
	)
	private void onPlayerLeaveTeamPost(CallbackInfo ci, @Share("events") LocalRef<List<Event>> events) {
		for (Event event : events.get()) {
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
		}
	}
}
