package net.casual.arcade.mixin.events;

import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.*;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/thread/ReentrantBlockableEventLoop;<init>(Ljava/lang/String;)V",
			shift = At.Shift.AFTER
		)
	)
	private void onCreateServerInstance(CallbackInfo ci) {
		ServerCreatedEvent event = new ServerCreatedEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "runServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;",
			shift = At.Shift.AFTER
		)
	)
	private void onServerLoaded(CallbackInfo ci) {
		ServerLoadedEvent event = new ServerLoadedEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "tickServer",
		at = @At("HEAD")
	)
	private void preTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		ServerTickEvent event = new ServerTickEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES);
	}

	@Inject(
		method = "tickServer",
		at = @At("TAIL")
	)
	private void postTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		ServerTickEvent event = new ServerTickEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}

	@Inject(
		method = "stopServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;saveAllChunks(ZZZ)Z"
		)
	)
	private void onSave(CallbackInfo ci) {
		ServerSaveEvent event = new ServerSaveEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "stopServer",
		at = @At("HEAD")
	)
	private void onShutdown(CallbackInfo ci) {
		ServerStoppingEvent event = new ServerStoppingEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "saveEverything",
		at = @At("TAIL")
	)
	private void onSaveEverything(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
		ServerSaveEvent event = new ServerSaveEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}
}
