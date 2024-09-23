package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerBlockInteractionEvent;
import net.casual.arcade.events.player.PlayerBlockMinedEvent;
import net.casual.arcade.events.player.PlayerBlockStartMiningEvent;
import net.casual.arcade.events.player.PlayerGameModeChangeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow @Final protected ServerPlayer player;

	@Shadow private GameType gameModeForPlayer;

	@Inject(
		method = "changeGameModeForPlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayerGameMode;setGameModeForPlayer(Lnet/minecraft/world/level/GameType;Lnet/minecraft/world/level/GameType;)V",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	private void onChangeGameMode(GameType gameModeForPlayer, CallbackInfoReturnable<Boolean> cir) {
		PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(this.player, this.gameModeForPlayer, gameModeForPlayer);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
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
	private void onInteractBlock(
		ServerPlayer player,
		Level level,
		ItemStack stack,
		InteractionHand hand,
		BlockHitResult hitResult,
		CallbackInfoReturnable<InteractionResult> cir,
		@Share("blockInteractionEvent") LocalRef<PlayerBlockInteractionEvent> eventRef
	) {
		PlayerBlockInteractionEvent event = new PlayerBlockInteractionEvent(player, stack, hand, hitResult);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(event.result());
		}
		eventRef.set(event);
	}

	@ModifyVariable(
		method = "useItemOn",
		at = @At(value = "STORE"),
		ordinal = 1
	)
	private boolean shouldPreventUsingOnBlock(
		boolean value,
		@Share("blockInteractionEvent") LocalRef<PlayerBlockInteractionEvent> eventRef
	) {
		PlayerBlockInteractionEvent event = eventRef.get();
		return event.getPreventUsingOnBlock() || value;
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
	private void onDestroyBlock(
		BlockPos pos,
		CallbackInfoReturnable<Boolean> cir,
		@Local BlockState state,
		@Local BlockEntity entity
	) {
		PlayerBlockMinedEvent event = new PlayerBlockMinedEvent(this.player, pos, state, entity);
		GlobalEventHandler.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}

	@ModifyExpressionValue(
		method = "handleBlockBreakAction",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;blockActionRestricted(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/GameType;)Z"
		)
	)
	private boolean isBreakingRestricted(
		boolean original,
		BlockPos pos,
		ServerboundPlayerActionPacket.Action action,
		Direction face
	) {
		if (original) {
			return true;
		}
		PlayerBlockStartMiningEvent event = new PlayerBlockStartMiningEvent(this.player, pos, face);
		GlobalEventHandler.broadcast(event);
		return event.isCancelled();
	}
}
