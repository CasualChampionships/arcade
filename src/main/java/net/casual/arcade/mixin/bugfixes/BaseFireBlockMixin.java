package net.casual.arcade.mixin.bugfixes;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.Minigames;
import net.casual.arcade.utils.BoundingBoxUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
			Minigame<?> minigame = Minigames.INSTANCE.byLevel(serverLevel);
			if (minigame != null && minigame.getSettings().getMobsWithNoAIAreFlammable()) {
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
