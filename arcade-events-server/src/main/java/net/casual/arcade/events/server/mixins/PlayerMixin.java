/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.mojang.datafixers.util.Either;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ducks.ModifyActuallyHurt;
import net.casual.arcade.events.server.entity.EntityDamageEvent;
import net.casual.arcade.events.server.player.*;
import net.casual.arcade.utils.player.PlayerUtilsKt;
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
import net.minecraft.world.phys.Vec3;
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
	private void onAttack(Entity entity, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerTryAttackEvent event = new PlayerTryAttackEvent(player, entity);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				ci.cancel();
			}
		}
	}

	@WrapOperation(
		method = "doSweepAttack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean onSweepAttack(LivingEntity entity, ServerLevel level, DamageSource source, float damage, Operation<Boolean> original) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerAttackEvent event = new PlayerAttackEvent(player, entity, damage);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				return false;
			}
			damage = event.getDamage();
		}
		return original.call(entity, level, source, damage);
	}

	@WrapOperation(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean onAttack(Entity entity, DamageSource source, float damage, Operation<Boolean> original) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerAttackEvent event = new PlayerAttackEvent(player, entity, damage);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				return false;
			}
			damage = event.getDamage();
		}
		return original.call(entity, source, damage);
	}

	@Inject(
		method = "interactOn",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onInteractOn(
		Entity entity,
		InteractionHand hand,
		Vec3 location,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerEntityInteractionEvent event = new PlayerEntityInteractionEvent(player, entity, hand, location);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
                PlayerUtilsKt.updateInteractionSlot(player, hand);
				cir.setReturnValue(event.result());
			}
		}
	}

    @Definition(id = "getDamageAfterMagicAbsorb", method = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F")
    @Expression("? = ?.getDamageAfterMagicAbsorb(?, ?)")
    @Inject(
		method = "actuallyHurt",
		at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
		cancellable = true
	)
	private void onDamage(
		ServerLevel level,
		DamageSource source,
		float damageAmount,
		CallbackInfo ci,
		@Local(name = "dmg", argsOnly = true) LocalFloatRef damage
	) {
		if ((Object) this instanceof ServerPlayer player) {
            // This overrides the LivingEntity method so we need to copy the logic here
            EntityDamageEvent entityEvent = new EntityDamageEvent(player, source, damage.get());
            GlobalEventHandler.Server.broadcast(entityEvent, BuiltInEventPhases.PRE_PHASES);
            if (entityEvent.isCancelled()) {
                this.arcade_setNotActuallyHurt();
                ci.cancel();
                return;
            }

			PlayerDamageEvent playerEvent = new PlayerDamageEvent(player, source, entityEvent.getAmount());
			GlobalEventHandler.Server.broadcast(playerEvent, BuiltInEventPhases.PRE_PHASES);
			if (playerEvent.isCancelled()) {
				this.arcade_setNotActuallyHurt();
				ci.cancel();
                return;
			}
			damage.set(playerEvent.getAmount());
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
		float dmg,
		CallbackInfo ci
	) {
		if ((Object) this instanceof ServerPlayer player) {
            EntityDamageEvent entityEvent = new EntityDamageEvent(player, source, dmg);
            GlobalEventHandler.Server.broadcast(entityEvent, BuiltInEventPhases.POST_PHASES);

			PlayerDamageEvent playerEvent = new PlayerDamageEvent(player, source, dmg);
			GlobalEventHandler.Server.broadcast(playerEvent, BuiltInEventPhases.POST_PHASES);
		}
	}

	@Inject(
		method = "startSleepInBed",
		at = @At("TAIL")
	)
	private void onStartSleeping(
		BlockPos pos,
		CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerSleepEvent event = new PlayerSleepEvent(player, pos);
			GlobalEventHandler.Server.broadcast(event);
		}
	}
}
