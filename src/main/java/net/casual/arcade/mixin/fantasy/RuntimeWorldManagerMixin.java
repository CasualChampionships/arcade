package net.casual.arcade.mixin.fantasy;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;

@Mixin(targets = "xyz.nucleoid.fantasy.RuntimeWorldManager")
public class RuntimeWorldManagerMixin {
	@Shadow @Final private MinecraftServer server;

	@Inject(
		method = "add",
		at = @At("TAIL")
	)
	private void onAddLevel(
		ResourceKey<Level> worldKey,
		RuntimeWorldConfig config,
		RuntimeWorld.Style style,
		CallbackInfoReturnable<RuntimeWorld> cir
	) {
		for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
			this.server.getCommands().sendCommands(player);
		}
	}

	@Inject(
		method = "delete",
		at = @At(
			value = "INVOKE",
			target = "Lxyz/nucleoid/fantasy/RemoveFromRegistry;remove(Lnet/minecraft/core/MappedRegistry;Lnet/minecraft/resources/ResourceLocation;)Z",
			shift = At.Shift.AFTER
		)
	)
	private void onDeleteLevel(ServerLevel world, CallbackInfo ci) {
		for (ServerPlayer player : this.server.getPlayerList().getPlayers()) {
			this.server.getCommands().sendCommands(player);
		}
	}
}
