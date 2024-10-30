package net.casual.arcade.minigame.mixins;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(
		method = "useOn",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canPlaceBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (context.getPlayer() instanceof ServerPlayer player) {
			Minigame minigame = MinigameUtils.getMinigame(player);
			if (minigame != null && !minigame.getSettings().canPlaceBlocks.get(player)) {
				cir.setReturnValue(InteractionResult.PASS);
			}
		}
	}

}
