package net.casual.arcade.minigame.mixins;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
	@Inject(
		method = "onPlace",
		at = @At("TAIL")
	)
	private void onPlaceFire(
		BlockState state,
		Level level,
		BlockPos pos,
		BlockState oldState,
		boolean movedByPiston,
		CallbackInfo ci
	) {
		if (level instanceof ServerLevel serverLevel) {
			Set<Minigame> minigames = MinigameUtils.getMinigames(serverLevel, pos);
			if (MinigameUtils.ifSingular(minigames, m -> m.getSettings().getMobsWithNoAIAreFlammable())) {
				List<Mob> entities = serverLevel.getEntitiesOfClass(Mob.class, AABB.of(new BoundingBox(pos)));
				for (Mob entity : entities) {
					if (entity.isNoAi()) {
						state.entityInside(level, pos, entity);
					}
				}
			}
		}
	}
}
