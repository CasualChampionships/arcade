package net.casualuhc.arcade.mixin.events;

import com.llamalad7.mixinextras.sugar.Local;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.PlayerBlockInteractionEvent;
import net.casualuhc.arcade.events.player.PlayerBlockMinedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Shadow @Final protected ServerPlayer player;

	@Inject(
		method = "useItemOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;",
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
		CallbackInfoReturnable<InteractionResult> cir
	) {
		PlayerBlockInteractionEvent event = new PlayerBlockInteractionEvent(player, level, stack, hand, hitResult);
		EventHandler.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(event.result());
		}
	}

	@Inject(
		method = "destroyBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V",
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
		EventHandler.broadcast(event);
		if (event.isCancelled()) {
			cir.setReturnValue(false);
		}
	}
}
