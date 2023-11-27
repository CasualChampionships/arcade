package net.casual.arcade.mixin.minigame;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(
		method = "mayUseItemAt",
		at = @At("HEAD"),
		cancellable = true
	)
	private void mayPlaceBlock(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			Minigame<?> minigame = MinigameUtils.getMinigame(player);
			if (minigame != null && !minigame.getSettings().getCanPlaceBlocks()) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(
		method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void mayDropItems(
		ItemStack itemStack,
		boolean includeThrowerName,
		CallbackInfoReturnable<ItemEntity> cir
	) {
		if ((Object) this instanceof ServerPlayer player) {
			Minigame<?> minigame = MinigameUtils.getMinigame(player);
			if (minigame != null && !minigame.getSettings().getCanThrowItems()) {
				cir.setReturnValue(null);
			}
		}
	}
}
