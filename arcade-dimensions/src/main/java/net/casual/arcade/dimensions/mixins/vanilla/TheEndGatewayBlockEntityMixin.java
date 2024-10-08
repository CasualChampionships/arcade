package net.casual.arcade.dimensions.mixins.vanilla;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TheEndGatewayBlockEntity.class)
public class TheEndGatewayBlockEntityMixin {
	@ModifyExpressionValue(
		method = "getPortalPosition",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"
		)
	)
	private ResourceKey<Level> replaceEndKey(ResourceKey<Level> original, ServerLevel level) {
		return VanillaLikeLevel.getEndDimensionFor(level, original);
	}
}
