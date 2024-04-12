package net.casual.arcade.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Shadow @Final public MinecraftServer server;

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

	@Inject(
		method = "drop(Z)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onDropItem(boolean dropStack, CallbackInfoReturnable<Boolean> cir) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canDropItems.get(player)) {
			cir.setReturnValue(false);
		}
	}

	@ModifyExpressionValue(
		method = "fudgeSpawnLocation",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;getSharedSpawnPos()Lnet/minecraft/core/BlockPos;"
		)
	)
	private BlockPos getSharedSpawnPosition(BlockPos original) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		ServerPlayer old = this.server.getPlayerList().getPlayer(player.getUUID());
		if (old == null) {
			return original;
		}
		Minigame<?> minigame = MinigameUtils.getMinigame(old);
		if (minigame != null) {
			BlockPos spawnPosition = minigame.getLevels().getSpawn().position(player);
			if (spawnPosition != null) {
				return spawnPosition;
			}
		}
		return original;
	}
}
