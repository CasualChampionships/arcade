package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmBlock.class)
public class FarmBlockMixin {
	@WrapWithCondition(
		method = "fallOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/FarmBlock;turnToDirt(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"
		)
	)
	private boolean onFallOnFarmland(Entity entity, BlockState state, Level level, BlockPos pos) {
		if (entity instanceof ServerPlayer player) {
			Minigame minigame = MinigameUtils.getMinigame(player);
			return minigame == null || minigame.getSettings().canInteractBlocks.get(player);
		}
		return true;
	}
}
