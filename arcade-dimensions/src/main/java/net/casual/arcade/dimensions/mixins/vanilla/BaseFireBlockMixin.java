/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.vanilla;

import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
	@Redirect(
		method = "inPortalDimension",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;dimension()Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private static ResourceKey<Level> getDimensionLike(Level instance) {
		return VanillaLikeLevel.getLikeDimension(instance);
	}
}
