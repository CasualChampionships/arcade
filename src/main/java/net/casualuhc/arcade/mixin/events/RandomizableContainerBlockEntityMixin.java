package net.casualuhc.arcade.mixin.events;

import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.PlayerLootEvent;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin {
	@Shadow protected abstract NonNullList<ItemStack> getItems();

	@Inject(
		method = "unpackLootTable",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootContext;)V",
			shift = At.Shift.AFTER
		)
	)
	private void afterLootGenerated(Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerLootEvent event = new PlayerLootEvent(serverPlayer, this.getItems(), (RandomizableContainerBlockEntity) (Object) this);
			EventHandler.broadcast(event);
		}
	}
}
