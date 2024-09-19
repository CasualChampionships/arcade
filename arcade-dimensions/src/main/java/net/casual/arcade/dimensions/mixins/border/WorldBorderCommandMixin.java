package net.casual.arcade.dimensions.mixins.border;

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
