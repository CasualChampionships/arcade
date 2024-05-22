package net.casual.arcade.mixin.advancements;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.PlayerAdvancements;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementMixin {
	@WrapWithCondition(
		method = "method_53639",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			remap = false
		)
	)
	private boolean onWarnNoAdvancement(Logger instance, String string, Object o1, Object o2) {
		return false;
	}
}
