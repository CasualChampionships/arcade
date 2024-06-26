package net.casual.arcade.mixin.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.utils.LevelUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	@ModifyExpressionValue(
		method = "getPortalDestination",
		at = {
			@At(
				value = "FIELD",
				target = "Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;"
			),
			@At(
				value = "FIELD",
				target = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;"
			)
		}
	)
	private ResourceKey<Level> replaceVanillaKey(ResourceKey<Level> original, ServerLevel level) {
		return LevelUtils.getReplacementDimensionFor(level, original);
	}
}
