package net.casual.arcade.mixin.worldborder;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casual.arcade.border.ArcadeBorder;
import net.minecraft.world.level.border.WorldBorder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(WorldBorder.class)
public class WorldBorderMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "NEW",
			target = "(Lnet/minecraft/world/level/border/WorldBorder;D)Lnet/minecraft/world/level/border/WorldBorder$StaticBorderExtent;"
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
