/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	@ModifyExpressionValue(
		method = "getPortalDestination",
		at = {
			@At(
                value = "FIELD",
                target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;",
                opcode = Opcodes.GETSTATIC
            ),
			@At(
				value = "FIELD",
				target = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;",
                opcode = Opcodes.GETSTATIC
			)
		}
	)
	private ResourceKey<Level> replaceVanillaKey(ResourceKey<Level> original, ServerLevel currentLevel) {
		return VanillaLikeLevel.getReplacementDestinationFor(currentLevel, original);
	}
}
