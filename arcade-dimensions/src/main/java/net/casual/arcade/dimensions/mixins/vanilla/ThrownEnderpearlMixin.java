/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlMixin {
	@ModifyExpressionValue(
		method = "canTeleport",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> getLikeDimension(ResourceKey<Level> original, Level level) {
		return VanillaLikeLevel.getLikeDimension(level);
	}
}
