package net.casual.arcade.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@ModifyExpressionValue(
		method = "respawn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"
		)
	)
	private ServerLevel getDefaultRespawnDimension(ServerLevel original, ServerPlayer player) {
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		if (minigame != null) {
			ServerLevel spawn = minigame.getLevels().getSpawn();
			if (spawn != null) {
				return spawn;
			}
		}
		return original;
	}
}
