package net.casual.arcade.dimensions.mixins.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.vanilla.VanillaLikeLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
	@Shadow public abstract ServerLevel serverLevel();

	@ModifyExpressionValue(
		method = "changeDimension",
		at = {
			@At(
				value = "FIELD",
				target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;"
			),
			@At(
				value = "FIELD",
				target = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;"
			)
		}
	)
	private ResourceKey<Level> replaceVanillaKey(ResourceKey<Level> original) {
		return VanillaLikeLevel.getReplacementDimensionFor(this.serverLevel(), original);
	}

	@Redirect(
		method = "triggerDimensionChangeTriggers",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension(ServerLevel instance) {
		return VanillaLikeLevel.getLikeDimension(instance);
	}

	@Redirect(
		method = "triggerDimensionChangeTriggers",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension(Level instance) {
		return VanillaLikeLevel.getLikeDimension(instance);
	}
}
