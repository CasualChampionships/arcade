package net.casual.arcade.mixin.minigame;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
	@Inject(
		method = "playerTouch",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canPlayerPickUp(Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer) {
			Minigame<?> minigame = MinigameUtils.getMinigame((ServerPlayer) player);
			if (minigame != null && !minigame.getSettings().getCanPickupItems()) {
				ci.cancel();
			}
		}
	}
}
