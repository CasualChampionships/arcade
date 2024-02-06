package net.casual.arcade.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@ModifyExpressionValue(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/TickRateManager;runsNormally()Z"
		)
	)
	private boolean isTicking(boolean original) {
		return MinigameUtils.isTicking((ServerLevel) (Object) this);
	}

	@ModifyExpressionValue(
		method = "method_31420",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/TickRateManager;isEntityFrozen(Lnet/minecraft/world/entity/Entity;)Z"
		)
	)
	private boolean isEntityFrozen(
		boolean original,
		TickRateManager manager,
		ProfilerFiller filler,
		Entity entity
	) {
		if (entity instanceof ServerPlayer player) {
			return !MinigameUtils.isTicking(player);
		}
		return !MinigameUtils.isTicking((ServerLevel) (Object) this);
	}
}
