package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.datafixers.util.Either;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ducks.ModifyActuallyHurt;
import net.casual.arcade.events.server.player.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements ModifyActuallyHurt {
	@Inject(
		method = "attack",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onAttack(Entity target, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerTryAttackEvent event = new PlayerTryAttackEvent(player, target);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				ci.cancel();
			}
		}
	}

	@WrapOperation(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"
		)
	)
	private void onSweepAttack(LivingEntity entity, DamageSource source, float amount, Operation<Void> original) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerAttackEvent event = new PlayerAttackEvent(player, entity, amount);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				return;
			}
			amount = event.getDamage();
		}
		original.call(entity, source, amount);
	}

	@WrapOperation(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean onAttack(Entity entity, DamageSource source, float amount, Operation<Boolean> original) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerAttackEvent event = new PlayerAttackEvent(player, entity, amount);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				return false;
			}
			amount = event.getDamage();
		}
		return original.call(entity, source, amount);
	}

	@Inject(
		method = "interactOn",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onInteractOn(
		Entity entity,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerEntityInteractionEvent event = new PlayerEntityInteractionEvent(player, entity, hand);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				cir.setReturnValue(event.result());
			}
		}
	}

	@Inject(
		method = "actuallyHurt",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onDamage(
		ServerLevel level,
		DamageSource source,
		float damageAmount,
		CallbackInfo ci,
		@Local(argsOnly = true) LocalFloatRef damage
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerDamageEvent event = new PlayerDamageEvent(player, source, damage.get());
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			if (event.isCancelled()) {
				this.arcade$setNotActuallyHurt();
				ci.cancel();
			}
			damage.set(event.getAmount());
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
		ServerLevel level,
		DamageSource source,
		float damageAmount,
		CallbackInfo ci
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerDamageEvent event = new PlayerDamageEvent(player, source, damageAmount);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
		}
	}

	@Inject(
		method = "startSleepInBed",
		at = @At("TAIL")
	)
	private void onStartSleeping(
		BlockPos bedPos,
		CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerSleepEvent event = new PlayerSleepEvent(player, bedPos);
			GlobalEventHandler.Server.broadcast(event);
		}
	}
}
