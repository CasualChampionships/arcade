package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@WrapWithCondition(
		method = "doClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;"
		)
	)
	private boolean canDropItem(Player instance, ItemStack itemStack, boolean includeThrowerName) {
		if (instance instanceof ServerPlayer player) {
			Minigame<?> minigame = MinigameUtils.getMinigame(player);
			return minigame == null || minigame.getSettings().canDropItems.get(player);
		}
		return true;
	}
}
