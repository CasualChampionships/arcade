package net.casual.arcade.mixin.events;

import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
	public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(level, blockPos, f, gameProfile);
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci) {
		PlayerTickEvent event = new PlayerTickEvent((ServerPlayer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "onInsideBlock",
		at = @At("HEAD")
	)
	private void onBlockCollision(BlockState state, CallbackInfo ci) {
		PlayerBlockCollisionEvent event = new PlayerBlockCollisionEvent((ServerPlayer) (Object) this, state);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "setServerLevel",
		at = @At("HEAD")
	)
	private void onChangeDimension(ServerLevel level, CallbackInfo ci) {
		PlayerDimensionChangeEvent event = new PlayerDimensionChangeEvent((ServerPlayer) (Object) this, level);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "doCheckFallDamage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
		)
	)
	private void onFall(double movementX, double movementY, double movementZ, boolean onGround, CallbackInfo ci) {
		PlayerFallEvent event = new PlayerFallEvent((ServerPlayer) (Object) this, movementY, onGround);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "die",
		at = @At("HEAD")
	)
	private void onDeathPre(DamageSource source, CallbackInfo ci) {
		PlayerDeathEvent event = new PlayerDeathEvent((ServerPlayer) (Object) this, source);
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES);
	}

	@Inject(
		method = "die",
		at = @At("TAIL")
	)
	private void onDeathPost(DamageSource source, CallbackInfo ci) {
		PlayerDeathEvent event = new PlayerDeathEvent((ServerPlayer) (Object) this, source);
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}

	@Override
	protected void updatePlayerPose() {
		Pose previous = this.getPose();
		super.updatePlayerPose();
		Pose updated = this.getPose();
		if (previous != updated) {
			PlayerPoseEvent event = new PlayerPoseEvent((ServerPlayer) (Object) this, previous, updated);
			GlobalEventHandler.broadcast(event);
		}
	}
}
