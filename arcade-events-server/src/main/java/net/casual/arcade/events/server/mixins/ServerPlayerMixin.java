/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.entity.EntityDeathEvent;
import net.casual.arcade.events.server.player.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level level, GameProfile gameProfile) {
		super(level, gameProfile);
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTickPre(CallbackInfo ci) {
		PlayerTickEvent event = new PlayerTickEvent((ServerPlayer) (Object) this);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
	}

	@Inject(
		method = "tick",
		at = @At("TAIL")
	)
	private void onTickPost(CallbackInfo ci) {
		PlayerTickEvent event = new PlayerTickEvent((ServerPlayer) (Object) this);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}

	@Inject(
		method = "onInsideBlock",
		at = @At("HEAD")
	)
	private void onBlockCollision(BlockState state, CallbackInfo ci) {
		PlayerBlockCollisionEvent event = new PlayerBlockCollisionEvent((ServerPlayer) (Object) this, state);
		GlobalEventHandler.Server.broadcast(event);
	}

	@Inject(
		method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;setServerLevel(Lnet/minecraft/server/level/ServerLevel;)V"
		)
	)
	private void onChangeDimension(
		TeleportTransition transition,
		CallbackInfoReturnable<Entity> cir
	) {
		ServerLevel level = transition.newLevel();
		if (this.level().dimension() != level.dimension()) {
			PlayerDimensionChangeEvent event = new PlayerDimensionChangeEvent((ServerPlayer) (Object) this, level);
			GlobalEventHandler.Server.broadcast(event);
		}
	}

	@Inject(
		method = "checkFallDamage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
		)
	)
	private void onFall(double y, boolean onGround, BlockState state, BlockPos pos, CallbackInfo ci) {
		PlayerFallEvent event = new PlayerFallEvent((ServerPlayer) (Object) this, y, onGround);
		GlobalEventHandler.Server.broadcast(event);
	}

	@Inject(
		method = "die",
		at = @At("HEAD")
	)
	private void onDeathPre(DamageSource source, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;

		// This doesn't call super.die, so we must manually call this event here
		EntityDeathEvent entityDeathEvent = new EntityDeathEvent(player, source);
		GlobalEventHandler.Server.broadcast(entityDeathEvent, BuiltInEventPhases.PRE_PHASES);

		PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent(player, source);
		GlobalEventHandler.Server.broadcast(playerDeathEvent, BuiltInEventPhases.PRE_PHASES);
	}

	@Inject(
		method = "die",
		at = @At("TAIL")
	)
	private void onDeathPost(DamageSource source, CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer) (Object) this;

		EntityDeathEvent entityDeathEvent = new EntityDeathEvent(player, source);
		GlobalEventHandler.Server.broadcast(entityDeathEvent, BuiltInEventPhases.POST_PHASES);

		PlayerDeathEvent playerDeathEvent = new PlayerDeathEvent(player, source);
		GlobalEventHandler.Server.broadcast(playerDeathEvent, BuiltInEventPhases.POST_PHASES);
	}

	@ModifyReturnValue(
		method = "canHarmPlayer",
		at = @At("RETURN")
	)
	private boolean onCanHarmPlayer(boolean original, Player other) {
		if (other instanceof ServerPlayer player) {
			PlayerTryHarmEvent event = new PlayerTryHarmEvent((ServerPlayer) (Object) this, player, original);
			GlobalEventHandler.Server.broadcast(event);
			return event.getCanHarmOtherBoolean();
		}
		return original;
	}

	@WrapOperation(
		method = "die",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private void onSendDeathMessage(
		PlayerList instance,
		Component message,
		boolean bypassHiddenChat,
		Operation<Void> original
	) {
		PlayerSystemMessageEvent.broadcast((ServerPlayer) (Object) this, instance, message, bypassHiddenChat, original);
	}



	@Inject(
		method = "jumpFromGround",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onJump(CallbackInfo ci) {
		PlayerJumpEvent event = new PlayerJumpEvent((ServerPlayer) (Object) this);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "jumpFromGround",
		at = @At("TAIL")
	)
	private void onJumpPost(CallbackInfo ci) {
		PlayerJumpEvent event = new PlayerJumpEvent((ServerPlayer) (Object) this);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}

	@Override
	protected void updatePlayerPose() {
		Pose previous = this.getPose();
		super.updatePlayerPose();
		Pose updated = this.getPose();
		if (previous != updated) {
			PlayerPoseEvent event = new PlayerPoseEvent((ServerPlayer) (Object) this, previous, updated);
			GlobalEventHandler.Server.broadcast(event);
		}
	}
}
