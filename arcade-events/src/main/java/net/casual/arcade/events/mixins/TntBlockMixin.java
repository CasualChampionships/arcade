package net.casual.arcade.events.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerTNTPrimedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntBlock.class)
public class TntBlockMixin {
	@Inject(
		method = "explode(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/LivingEntity;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void onPrime(Level level, BlockPos pos, LivingEntity entity, CallbackInfo ci) {
		if (entity instanceof ServerPlayer player) {
			PlayerTNTPrimedEvent event = new PlayerTNTPrimedEvent(player, (ServerLevel) level, pos);
			GlobalEventHandler.broadcast(event);
			if (event.isCancelled()) {
				ci.cancel();
			}
		}
 	}
}
