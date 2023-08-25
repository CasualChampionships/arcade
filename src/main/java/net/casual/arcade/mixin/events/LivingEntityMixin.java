package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerBorderDamageEvent;
import net.casual.arcade.events.player.PlayerDeathEvent;
import net.casual.arcade.events.player.PlayerLandEvent;
import net.casual.arcade.events.player.PlayerVoidDamageEvent;
import net.casual.arcade.utils.CastUtils;
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
		ServerPlayer player = CastUtils.tryCast(ServerPlayer.class, this);
		if (player == null) {
			return;
		}
		PlayerLandEvent event = new PlayerLandEvent(player, damage.get(), distance, multiplier, source);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			damage.set(event.result());
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
		ServerPlayer player = CastUtils.tryCast(ServerPlayer.class, this);
		if (player == null) {
			return true;
		}
		PlayerVoidDamageEvent event = new PlayerVoidDamageEvent(player);
		GlobalEventHandler.broadcast(event);
		return !event.isCancelled();
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
		ServerPlayer player = CastUtils.tryCast(ServerPlayer.class, this);
		if (player == null) {
			return true;
		}
		PlayerBorderDamageEvent event = new PlayerBorderDamageEvent(player, source, amount);
		GlobalEventHandler.broadcast(event);
		return !event.isCancelled();
	}

	@WrapWithCondition(
		method = "hurt",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"
		)
	)
	private boolean onDeath(LivingEntity instance, DamageSource source) {
		ServerPlayer player = CastUtils.tryCast(ServerPlayer.class, this);
		if (player == null) {
			return true;
		}
		PlayerDeathEvent event = new PlayerDeathEvent(player, source);
		GlobalEventHandler.broadcast(event);
		return !event.isCancelled();
	}
}
