/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.mixins;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.WorldBorderCommand;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldBorderCommand.class)
public class WorldBorderCommandMixin {
	@ModifyVariable(
		method = {
			"setDamageBuffer",
			"setDamageAmount",
			"setWarningTime",
			"setWarningDistance",
			"getSize",
			"setCenter",
			"setSize"
		},
		at = @At("STORE")
	)
	private static WorldBorder onGetBorder(WorldBorder original, CommandSourceStack source) {
		return source.getLevel().getWorldBorder();
	}
}
