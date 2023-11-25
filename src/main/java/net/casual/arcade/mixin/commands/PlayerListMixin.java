package net.casual.arcade.mixin.commands;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Shadow public abstract void sendPlayerPermissionLevel(ServerPlayer player);

	@WrapWithCondition(
		method = "respawn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V"
		)
	)
	private boolean shouldSendPermissions(PlayerList instance, ServerPlayer player) {
		return false;
	}

	@Inject(
		method = "respawn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;addRespawnedPlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
			shift = At.Shift.AFTER
		)
	)
	private void afterAddRespawnPlayer(
		ServerPlayer player,
		boolean keepEverything,
		CallbackInfoReturnable<ServerPlayer> cir
	) {
		this.sendPlayerPermissionLevel(player);
	}
}
