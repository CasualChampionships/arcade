/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.phase.BuiltInEventPhases;
import net.casual.arcade.events.server.player.*;
import net.casual.arcade.utils.player.PlayerUtilsKt;
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
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow @Final protected ServerPlayer player;
	@Shadow private GameType gameModeForPlayer;
    @Shadow protected ServerLevel level;

    @Inject(
		method = "changeGameModeForPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	@SuppressWarnings("DiscouragedShift")
	private void onChangeGameMode(GameType gameModeForPlayer, CallbackInfoReturnable<Boolean> cir) {
		PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(this.player, this.gameModeForPlayer, gameModeForPlayer);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(
		method = "useItem",
		at = @At("HEAD"),
		cancellable = true
	)
	private void broadcastItemUseEvent(
		ServerPlayer player,
		Level level,
		ItemStack itemStack,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		PlayerItemUseEvent event = new PlayerItemUseEvent(player, itemStack, hand);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES_RAW);
		if (event.isCancelled()) {
			cir.setReturnValue(event.result());
		}
	}

	@Inject(
		method = "useItemOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	@SuppressWarnings("DiscouragedShift")
	private void onInteractBlock(
		ServerPlayer player,
		Level level,
		ItemStack itemStack,
		InteractionHand hand,
		BlockHitResult hitResult,
		CallbackInfoReturnable<InteractionResult> cir,
		@Share("blockInteractionEvent") LocalRef<PlayerBlockInteractionEvent> eventRef
	) {
		PlayerBlockInteractionEvent event = new PlayerBlockInteractionEvent(player, itemStack, hand, hitResult);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
            PlayerUtilsKt.updateInteractionSlot(player, hand);
			cir.setReturnValue(event.result());
		}
		eventRef.set(event);
	}

	@ModifyVariable(
		method = "useItemOn",
		at = @At(value = "STORE"),
		name = "suppressUsingBlock"
	)
	private boolean shouldPreventUsingOnBlock(
		boolean suppressUsingBlock,
		@Share("blockInteractionEvent") LocalRef<PlayerBlockInteractionEvent> eventRef
	) {
		PlayerBlockInteractionEvent event = eventRef.get();
		return event.getPreventUsingOnBlock() || suppressUsingBlock;
	}

	@Inject(
		method = "destroyBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	@SuppressWarnings("DiscouragedShift")
	private void onDestroyBlock(
		BlockPos pos,
		CallbackInfoReturnable<Boolean> cir,
		@Local(name = "state") BlockState state,
		@Local(name = "blockEntity") BlockEntity blockEntity
	) {
		PlayerBlockMinedEvent event = new PlayerBlockMinedEvent(this.player, pos, state, blockEntity);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(
		method = "handleBlockBreakAction",
		at = @At("HEAD"),
        cancellable = true
    )
	private void onBlockStartMining(
        BlockPos pos,
        ServerboundPlayerActionPacket.Action action,
        Direction direction,
        int maxY,
        int sequence,
        CallbackInfo ci
	) {
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
            PlayerBlockStartMiningEvent event = new PlayerBlockStartMiningEvent(this.player, pos, direction);
            GlobalEventHandler.Server.broadcast(event);
            if (event.isCancelled()) {
                this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
                ci.cancel();
            }
        }
	}
}
