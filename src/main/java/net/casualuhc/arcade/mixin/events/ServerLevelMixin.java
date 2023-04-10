package net.casualuhc.arcade.mixin.events;

import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.level.LevelBlockChangedEvent;
import net.casualuhc.arcade.events.level.LevelTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		LevelTickEvent event = new LevelTickEvent((ServerLevel) (Object) this);
		EventHandler.broadcast(event);
	}

	@Inject(
		method = "onBlockStateChange",
		at = @At("HEAD")
	)
	private void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		LevelBlockChangedEvent event = new LevelBlockChangedEvent((ServerLevel) (Object) this, pos, oldState, newState);
		EventHandler.broadcast(event);
	}
}
