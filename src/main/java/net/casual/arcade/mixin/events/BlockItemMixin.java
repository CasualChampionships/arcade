package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerBlockPlacedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@WrapOperation(
		method = "place",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"
		)
	)
	private boolean onPlaceBlock(BlockItem instance, BlockPlaceContext context, BlockState state, Operation<Boolean> operation) {
		if (context.getPlayer() instanceof ServerPlayer player) {
			PlayerBlockPlacedEvent event = new PlayerBlockPlacedEvent(player, instance, state, context);
			GlobalEventHandler.broadcast(event);
			if (event.isCancelled()) {
				return false;
			}
		}
		return operation.call(instance, context, state);
	}
}
