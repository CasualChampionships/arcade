package net.casual.arcade.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		return original && (minigame == null || minigame.getSettings().canPvp.get(player));
	}

	@Inject(
		method = "isInvulnerableTo",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onIsInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canTakeDamage.get(player)) {
			cir.setReturnValue(true);
		}
	}
}
