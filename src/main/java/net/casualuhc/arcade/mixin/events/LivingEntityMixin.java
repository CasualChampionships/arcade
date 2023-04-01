package net.casualuhc.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.PlayerBorderDamageEvent;
import net.casualuhc.arcade.events.player.PlayerFallEvent;
import net.casualuhc.arcade.events.player.PlayerVoidDamageEvent;
import net.casualuhc.arcade.utils.CastUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
		PlayerFallEvent event = new PlayerFallEvent(player, damage.get(), distance, multiplier, source);
		EventHandler.broadcast(event);
		if (event.isCancelled()) {
			damage.set(event.getNewDamage());
		}
	}

	@WrapWithCondition(
		method = "outOfWorld",
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
		EventHandler.broadcast(event);
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
		EventHandler.broadcast(event);
		return !event.isCancelled();
	}
}
