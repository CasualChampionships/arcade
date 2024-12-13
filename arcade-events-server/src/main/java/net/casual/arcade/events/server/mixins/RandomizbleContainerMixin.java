package net.casual.arcade.events.server.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.level.LevelLootEvent;
import net.casual.arcade.events.server.player.PlayerLootEvent;
import net.casual.arcade.utils.InventoryUtilsKt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RandomizableContainer.class)
public interface RandomizbleContainerMixin extends Container {
	@Shadow @Nullable Level getLevel();

	@Shadow BlockPos getBlockPos();

	@Inject(
		method = "unpackLootTable",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;fill(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/storage/loot/LootParams;J)V",
			shift = At.Shift.AFTER
		)
	)
	private void afterLootGenerated(Player player, CallbackInfo ci) {
		List<ItemStack> items = InventoryUtilsKt.getAllItems(this);
		RandomizableContainer container = (RandomizableContainer) this;

		LevelLootEvent levelLootEvent = new LevelLootEvent((ServerLevel) this.getLevel(), this.getBlockPos(), items, container);
		GlobalEventHandler.Server.broadcast(levelLootEvent);
		if (player instanceof ServerPlayer serverPlayer) {
			PlayerLootEvent playerLootEvent = new PlayerLootEvent(serverPlayer, items, container);
			GlobalEventHandler.Server.broadcast(playerLootEvent);
		}
	}
}
