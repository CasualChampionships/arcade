package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.level.LevelFluidTrySpreadEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlowingFluid.class)
public class FlowingFluidMixin {
	@ModifyReturnValue(
		method = "canMaybePassThrough",
		at = @At("RETURN")
	)
	private boolean onCanSpreadTo(
		boolean original,
		BlockGetter level,
		BlockPos fromPos,
		BlockState fromBlockState,
		Direction direction,
		BlockPos toPos,
		BlockState toBlockState,
		FluidState toFluidState
	) {
		if (level instanceof ServerLevel serverLevel) {
			LevelFluidTrySpreadEvent event = new LevelFluidTrySpreadEvent(
				serverLevel,
				fromPos,
				fromBlockState,
				direction,
				toPos,
				toBlockState,
				toFluidState,
				original
			);
			GlobalEventHandler.broadcast(event);
			return event.getCanSpread();
		}
		return original;
	}
}
