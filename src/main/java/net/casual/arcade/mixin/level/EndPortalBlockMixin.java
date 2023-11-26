package net.casual.arcade.mixin.level;

import net.casual.arcade.utils.LevelUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
	@ModifyVariable(
		method = "entityInside",
		at = @At("STORE")
	)
	private ResourceKey<Level> getOppositeDimension(ResourceKey<Level> original, BlockState state, Level level) {
		return LevelUtils.getEndOppositeDimension(level, original);
	}
}
