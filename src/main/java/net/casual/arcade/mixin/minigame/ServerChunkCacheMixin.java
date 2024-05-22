package net.casual.arcade.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
	@Shadow @Final ServerLevel level;

	@ModifyExpressionValue(
		method = "tickChunks",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/TickRateManager;runsNormally()Z"
		)
	)
	private boolean isTicking(boolean original) {
		return MinigameUtils.isTicking(this.level);
	}
}
