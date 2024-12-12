package net.casual.arcade.events.server.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.level.LevelBlockChangedEvent;
import net.casual.arcade.events.server.level.LevelTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		LevelTickEvent event = new LevelTickEvent((ServerLevel) (Object) this);
		GlobalEventHandler.Server.broadcast(event);
	}

	@Inject(
		method = "onBlockStateChange",
		at = @At("HEAD")
	)
	private void onBlockChanged(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		LevelBlockChangedEvent event = new LevelBlockChangedEvent((ServerLevel) (Object) this, pos, oldState, newState);
		if (this.server.isSameThread()) {
			GlobalEventHandler.Server.broadcast(event);
		} else {
			this.server.execute(() -> GlobalEventHandler.Server.broadcast(event));
		}
	}
}
