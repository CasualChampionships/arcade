/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
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
		Minigame minigame = MinigameUtils.getMinigame(this.player);
		if (minigame != null && !minigame.getSettings().canBreakBlocks.get(this.player)) {
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
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canInteractItems.get(player)) {
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
		Minigame minigame = MinigameUtils.getMinigame(player);
		if (minigame != null && !minigame.getSettings().canInteractBlocks.get(player)) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
