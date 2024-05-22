package net.casual.arcade.mixin.events;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerLootEvent;
import net.casual.arcade.utils.InventoryUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainer.class)
public interface RandomizbleContainerMixin extends Container {
	@Inject(
		method = "unpackLootTable",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootParams;J)V",
			shift = At.Shift.AFTER
		)
	)
	private void afterLootGenerated(Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerLootEvent event = new PlayerLootEvent(serverPlayer, InventoryUtils.getAllItems(this), (RandomizableContainerBlockEntity) this);
			GlobalEventHandler.broadcast(event);
		}
	}
}
