package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerAttackEvent;
import net.casual.arcade.events.player.PlayerDamageEvent;
import net.casual.arcade.events.player.PlayerJumpEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
	@Redirect(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean onSweepAttack(LivingEntity entity, DamageSource source, float amount) {
		return this.onAttack(entity, source, amount);
	}

	@Redirect(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean onAttack(Entity entity, DamageSource source, float amount) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerAttackEvent event = new PlayerAttackEvent(player, entity, amount);
			GlobalEventHandler.broadcast(event);
			if (event.isCancelled()) {
				return false;
			}
		}
		return entity.hurt(source, amount);
	}

	@Inject(
		method = "actuallyHurt",
		at = @At(
			value = "INVOKE",
			target = "Ljava/lang/Math;max(FF)F",
			shift = At.Shift.AFTER,
			remap = false
		)
	)
	private void onDamage(DamageSource source, float damageAmount, CallbackInfo ci, @Local(ordinal = 1) LocalFloatRef damage) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerDamageEvent event = new PlayerDamageEvent(player, damage.get(), source);
			GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			if (event.isCancelled()) {
				damage.set(event.result());
			}
		}
	}

	@Inject(
		method = "actuallyHurt",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;gameEvent(Lnet/minecraft/core/Holder;)V",
			shift = At.Shift.AFTER
		)
	)
	private void onDamagePost(
		DamageSource source,
		float damageAmount,
		CallbackInfo ci,
		@Local(ordinal = 1)
		float damage
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerDamageEvent event = new PlayerDamageEvent(player, damage, source);
			GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES);
		}
	}

	@Inject(
		method = "jumpFromGround",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onJump(CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerJumpEvent event = new PlayerJumpEvent(player);
			GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			if (event.isCancelled()) {
				ci.cancel();
			}
		}
	}

	@Inject(
		method = "jumpFromGround",
		at = @At("TAIL")
	)
	private void onJumpPost(CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerJumpEvent event = new PlayerJumpEvent(player);
			GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES);
		}
	}
}
