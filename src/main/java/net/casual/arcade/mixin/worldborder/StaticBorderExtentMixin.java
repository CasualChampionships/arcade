package net.casual.arcade.mixin.worldborder;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casual.arcade.border.ArcadeBorder;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldBorder.StaticBorderExtent.class)
public class StaticBorderExtentMixin {
	@Shadow @Final WorldBorder field_12748;

	@WrapWithCondition(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder$StaticBorderExtent;updateBox()V"
		)
	)
	private boolean onUpdateBox(WorldBorder.StaticBorderExtent instance) {
		// Prevent NullPointerException, updateBox() leaks `this`
		return !(this.field_12748 instanceof ArcadeBorder);
	}
}
