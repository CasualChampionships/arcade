package net.casual.arcade.events.server.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.block.BrewingStandBrewEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
	@ModifyVariable(
		method = "serverTick",
		ordinal = 0,
		at = @At(
			value = "LOAD",
			ordinal = 2
		)
	)
	private static boolean onBrew(
		boolean value,
		Level level,
		BlockPos pos,
		BlockState state,
		BrewingStandBlockEntity entity
	) {
		if (!value) {
			return false;
		}
		if (((BrewingStandBlockEntityAccessor) entity).fuel() <= 0) {
			return false;
		}
		BrewingStandBrewEvent event = new BrewingStandBrewEvent((ServerLevel) level, pos, state, entity);
		GlobalEventHandler.Server.broadcast(event);
		return !event.isCancelled();
	}
}
