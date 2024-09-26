package net.casual.arcade.dimensions.mixins.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
	@ModifyExpressionValue(
		method = "getPortalDestination",
		at = {
			@At(
				value = "FIELD",
				target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
			),
			@At(
				value = "FIELD",
				target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;"
			)
		}
	)
	private ResourceKey<Level> replaceVanillaKey(ResourceKey<Level> original, ServerLevel level) {
		return VanillaLikeLevel.getReplacementDimensionFor(level, original);
	}
}
