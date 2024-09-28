package net.casual.arcade.minigame.mixins.gamemode;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.forceSetGameMode;
import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(
		method = "respawn",
		at = @At("RETURN")
	)
	private void onRespawnPlayer(CallbackInfoReturnable<ServerPlayer> cir) {
		ServerPlayer player = cir.getReturnValue();
		forceSetGameMode(player, getExtendedGameMode(player));
	}
}
