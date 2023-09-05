package net.casual.arcade.mixin.worldborder;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casual.arcade.border.ArcadeBorder;
import net.minecraft.world.level.border.WorldBorder;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(WorldBorder.StaticBorderExtent.class)
public class WorldBorderMixin {
	@Shadow
	@Final
	WorldBorder field_12748;

	@WrapWithCondition(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/border/WorldBorder$StaticBorderExtent;updateBox()V"
			)
	)
	private boolean onUpdateBox(WorldBorder.StaticBorderExtent instance) {
		return !(this.field_12748 instanceof ArcadeBorder);
	}


}



