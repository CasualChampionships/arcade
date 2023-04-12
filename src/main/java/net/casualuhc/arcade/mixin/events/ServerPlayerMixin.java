package net.casualuhc.arcade.mixin.events;

import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(CallbackInfo ci) {
		PlayerTickEvent event = new PlayerTickEvent((ServerPlayer) (Object) this);
		EventHandler.broadcast(event);
	}

	@Inject(
		method = "disconnect",
		at = @At("HEAD")
	)
	private void onDisconnect(CallbackInfo ci) {
		PlayerLeaveEvent event = new PlayerLeaveEvent((ServerPlayer) (Object) this);
		EventHandler.broadcast(event);
	}

	@Inject(
		method = "onInsideBlock",
		at = @At("HEAD")
	)
	private void onBlockCollision(BlockState state, CallbackInfo ci) {
		PlayerBlockCollisionEvent event = new PlayerBlockCollisionEvent((ServerPlayer) (Object) this, state);
		EventHandler.broadcast(event);
	}

	@Inject(
		method = "changeDimension",
		at = @At("RETURN")
	)
	private void onChangeDimension(ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
		PlayerDimensionChangeEvent event = new PlayerDimensionChangeEvent((ServerPlayer) (Object) this, destination);
		EventHandler.broadcast(event);
	}

	@Inject(
		method = "doCheckFallDamage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"
		)
	)
	private void onFall(double y, boolean onGround, CallbackInfo ci) {
		PlayerFallEvent event = new PlayerFallEvent((ServerPlayer) (Object) this, y, onGround);
		EventHandler.broadcast(event);
	}
}
