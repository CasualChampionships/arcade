package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerBorderDamageEvent;
import net.casual.arcade.events.player.PlayerLandEvent;
import net.casual.arcade.events.player.PlayerVoidDamageEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(
		method = "causeFallDamage",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(FF)I",
			shift = At.Shift.AFTER
		)
	)
	private void onFallDamage(float distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir, @Local LocalIntRef damage) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerLandEvent event = new PlayerLandEvent(player, damage.get(), distance, multiplier, source);
			GlobalEventHandler.broadcast(event);
			if (event.isCancelled()) {
				damage.set(event.result());
			}
		}
	}

	@WrapWithCondition(
		method = "onBelowWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
		)
	)
	private boolean onVoidDamage(LivingEntity instance, DamageSource source, float amount) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerVoidDamageEvent event = new PlayerVoidDamageEvent(player);
			GlobalEventHandler.broadcast(event);
			return !event.isCancelled();
		}
		return true;
	}

	@WrapWithCondition(
		method = "baseTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			ordinal = 1
		)
	)
	private boolean onHurtByBorder(LivingEntity instance, DamageSource source, float amount) {
		if ((Object) this instanceof ServerPlayer player) {
			PlayerBorderDamageEvent event = new PlayerBorderDamageEvent(player, source, amount);
			GlobalEventHandler.broadcast(event);
			return !event.isCancelled();
		}
		return true;
	}
}
