/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ducks.ModifyActuallyHurt;
import net.casual.arcade.events.server.entity.EntityDeathEvent;
import net.casual.arcade.events.server.player.*;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements ModifyActuallyHurt {
	@Unique private boolean arcade$wasActuallyHurt = false;

	@Inject(
		method = "causeFallDamage",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(DF)I",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	@SuppressWarnings("DiscouragedShift")
	private void onFallDamage(
		double distance,
		float multiplier,
		DamageSource source,
		CallbackInfoReturnable<Boolean> cir,
		@Local LocalIntRef damage
	) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerLandEvent event = new PlayerLandEvent(player, damage.get(), distance, multiplier, source);
			GlobalEventHandler.Server.broadcast(event);
			if (event.isCancelled()) {
				cir.setReturnValue(false);
			}
			damage.set(event.getDamage());
		}
	}

	@WrapWithCondition(
		method = "onBelowWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"
		)
	)
	private boolean onVoidDamage(LivingEntity instance, DamageSource source, float amount) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerVoidDamageEvent event = new PlayerVoidDamageEvent(player);
			GlobalEventHandler.Server.broadcast(event);
			return !event.isCancelled();
		}
		return true;
	}

	@WrapOperation(
		method = "hurtServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V"
		)
	)
	private void onHurt(
		LivingEntity instance,
		ServerLevel level,
		DamageSource damageSource,
		float damageAmount,
		Operation<Void> original,
		@Cancellable CallbackInfoReturnable<Boolean> cir
	) {
		this.arcade$wasActuallyHurt = true;
		original.call(instance, level, damageSource, damageAmount);
		if (!this.arcade$wasActuallyHurt) {
			cir.setReturnValue(false);
		}
	}

	@WrapOperation(
		method = "heal",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"
		)
	)
	private void onHeal(
		LivingEntity instance,
		float health,
		Operation<Void> original,
		float healAmount
	) {
		if (instance instanceof ServerPlayer player) {
			PlayerHealEvent event = new PlayerHealEvent(player, healAmount);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
			if (event.isCancelled()) {
				return;
			}
			original.call(instance, health);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
			return;
		}

		original.call(instance, health);
	}

	@Inject(
		method = "die",
		at = @At("HEAD")
	)
	private void onDeathPre(DamageSource source, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (!entity.level().isClientSide()) {
			EntityDeathEvent event = new EntityDeathEvent(entity, source);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		}
	}

	@Inject(
		method = "die",
		at = @At("TAIL")
	)
	private void onDeathPost(DamageSource source, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		if (!entity.level().isClientSide()) {
			EntityDeathEvent event = new EntityDeathEvent(entity, source);
			GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
		}
	}

	@Inject(
		method = "checkTotemDeathProtection",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V",
			shift = At.Shift.AFTER
		)
	)
	private void onEntityPoppedTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerTotemEvent event = new PlayerTotemEvent(player, source);
			GlobalEventHandler.Server.broadcast(event);
		}
	}

	@Inject(
		method = "onAttributeUpdated",
		at = @At("HEAD")
	)
	private void onAttributeUpdate(Holder<Attribute> attribute, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerAttributeUpdatedEvent event = new PlayerAttributeUpdatedEvent(player, attribute);
			GlobalEventHandler.Server.broadcast(event);
		}
	}

	@Override
	public void arcade$setNotActuallyHurt() {
		this.arcade$wasActuallyHurt = false;
	}
}
