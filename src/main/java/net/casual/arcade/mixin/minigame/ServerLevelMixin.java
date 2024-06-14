package net.casual.arcade.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Shadow @Final private ServerLevelData serverLevelData;

	@Shadow @Final private MinecraftServer server;

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
		return !MinigameUtils.isTicking(entity);
	}

	@ModifyArg(
		method = "tickTime",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"
		)
	)
	private long onSetDayTime(long time) {
		Minigame<?> minigame = MinigameUtils.getMinigame((ServerLevel) (Object) this);
		if (minigame == null) {
			return time;
		}
		int speed = minigame.getSettings().getDaylightCycle();
		long newTime = this.serverLevelData.getDayTime() + speed;
		if (speed > 1 && this.server.getTickCount() % 20 != 0) {
			minigame.getPlayers().broadcast(new ClientboundSetTimePacket(
				this.serverLevelData.getGameTime(), newTime, true
			));
		}
		return newTime;
	}
}
