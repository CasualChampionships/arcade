package net.casualuhc.arcade.mixin.events;

import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.server.ServerLoadedEvent;
import net.casualuhc.arcade.events.server.ServerStoppedEvent;
import net.casualuhc.arcade.events.server.ServerTickEvent;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(
		method = "loadLevel",
		at = @At("HEAD")
	)
	private void onServerLoaded(CallbackInfo ci) {
		ServerLoadedEvent event = new ServerLoadedEvent((MinecraftServer) (Object) this);
		EventHandler.broadcast(event);
	}

	@Inject(
		method = "tickServer",
		at = @At("TAIL")
	)
	private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		ServerTickEvent event = new ServerTickEvent((MinecraftServer) (Object) this);
		EventHandler.broadcast(event);
	}

	@Inject(
		method = "stopServer",
		at = @At("TAIL")
	)
	private void onShutdown(CallbackInfo ci) {
		ServerStoppedEvent event = new ServerStoppedEvent((MinecraftServer) (Object) this);
		EventHandler.broadcast(event);
	}
}
