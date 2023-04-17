package net.casualuhc.arcade.mixin.events;

import com.mojang.datafixers.DataFixer;
import net.casualuhc.arcade.events.GlobalEventHandler;
import net.casualuhc.arcade.events.server.ServerCreatedEvent;
import net.casualuhc.arcade.events.server.ServerLoadedEvent;
import net.casualuhc.arcade.events.server.ServerStoppedEvent;
import net.casualuhc.arcade.events.server.ServerTickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateServerInstance(
		Thread thread,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		PackRepository packRepository,
		WorldStem worldStem,
		Proxy proxy,
		DataFixer dataFixer,
		Services services,
		ChunkProgressListenerFactory chunkProgressListenerFactory,
		CallbackInfo ci
	) {
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
		at = @At("TAIL")
	)
	private void onTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		ServerTickEvent event = new ServerTickEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "stopServer",
		at = @At("TAIL")
	)
	private void onShutdown(CallbackInfo ci) {
		ServerStoppedEvent event = new ServerStoppedEvent((MinecraftServer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}
}
