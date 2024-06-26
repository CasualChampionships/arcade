package net.casual.arcade.mixin.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.utils.LevelUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlMixin {
	@ModifyExpressionValue(
		method = "canChangeDimensions",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension(ResourceKey<Level> original, Level level) {
		return LevelUtils.getLikeDimension(level);
	}
}
