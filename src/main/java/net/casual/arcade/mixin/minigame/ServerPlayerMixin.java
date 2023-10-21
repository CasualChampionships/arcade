package net.casual.arcade.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@ModifyExpressionValue(
		method = "isPvpAllowed",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;isPvpAllowed()Z"
		)
	)
	private boolean isPvpAllowed(boolean original) {
		Minigame<?> minigame = MinigameUtils.getMinigame((ServerPlayer) (Object) this);
		return original && (minigame == null || minigame.getPvp());
	}
}
