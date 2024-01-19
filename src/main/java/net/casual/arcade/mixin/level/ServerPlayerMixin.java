package net.casual.arcade.mixin.level;

import net.casual.arcade.utils.LevelUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Redirect(
		method = "findDimensionEntryPoint",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension0(ServerLevel instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@Redirect(
		method = "findDimensionEntryPoint",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension1(Level instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@Redirect(
		method = "changeDimension",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"
		),
		slice = @Slice(
			from = @At("HEAD"),
			to = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/server/level/ServerPlayer;unRide()V"
			)
		)
	)
	private ResourceKey<Level> getLikeDimension2(ServerLevel instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@Redirect(
		method = "changeDimension",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"
		),
		slice = @Slice(
			from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/server/level/ServerPlayer;findDimensionEntryPoint(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/level/portal/PortalInfo;"
			),
			to = @At("TAIL")
		)
	)
	private ResourceKey<Level> getLikeDimension3(ServerLevel instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@Redirect(
		method = "triggerDimensionChangeTriggers",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension4(ServerLevel instance) {
		return LevelUtils.getLikeDimension(instance);
	}

	@Redirect(
		method = "triggerDimensionChangeTriggers",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension4(Level instance) {
		return LevelUtils.getLikeDimension(instance);
	}
}
