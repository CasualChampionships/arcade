package net.casual.arcade.mixin.worldborder;

import net.casual.arcade.border.ArcadeBorder;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldBorder.class)
public class WorldBorderMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "NEW",
			target = "Lnet/minecraft/world/level/border/WorldBorder$StaticBorderExtent;<init>(Lnet/minecraft/world/level/border/WorldBorder;D)V"
		)
	)
	@SuppressWarnings("ConstantValue")
	private WorldBorder.StaticBorderExtent createExtent(WorldBorder border, double size) {
		if ((Object) this instanceof ArcadeBorder) {
			return null;
		}
		return border.new StaticBorderExtent(size);
	}
}
