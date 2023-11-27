package net.casual.arcade.mixin.minigame;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow @Final protected ServerPlayer player;

	@Shadow protected ServerLevel level;

	@Inject(
		method = "handleBlockBreakAction",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onBreakingBlock(
		BlockPos pos,
		ServerboundPlayerActionPacket.Action action,
		Direction face,
		int maxBuildHeight,
		int sequence,
		CallbackInfo ci
	) {
		Minigame<?> minigame = MinigameUtils.getMinigame(this.player);
		if (minigame != null && !minigame.getSettings().getCanBreakBlocks()) {
			this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
			ci.cancel();
		}
	}

	@Inject(
		method = "useItem",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canInteractItem(
		ServerPlayer player,
		Level level,
		ItemStack stack,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().getCanInteractItems()) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}

	@Inject(
		method = "useItemOn",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canInteractBlock(
		ServerPlayer player,
		Level level,
		ItemStack stack,
		InteractionHand hand,
		BlockHitResult hitResult,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		Minigame<?> minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().getCanInteractBlocks()) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
