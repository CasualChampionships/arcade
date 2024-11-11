package net.casual.arcade.minigame.mixins;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
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
	private void mayPlaceBlock(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			Minigame minigame = MinigameUtils.getMinigame(player);
			if (minigame != null && !minigame.getSettings().canPlaceBlocks.get(player)) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(
		method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void mayDropItems(CallbackInfoReturnable<ItemEntity> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			Minigame minigame = MinigameUtils.getMinigame(player);
			if (minigame != null && !minigame.getSettings().canDropItems.get(player)) {
				cir.setReturnValue(null);
			}
		}
	}
}
