package net.casual.arcade.dimensions.mixins.level.spawning;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.casual.arcade.dimensions.ducks.CustomMobSpawningPredicate;
import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.level.LocalMobCapCalculator$MobCounts")
public class MobCountsMixin implements CustomMobSpawningPredicate {
	@Shadow @Final private Object2IntMap<MobCategory> counts;

	@Override
	public boolean arcade$canSpawn(MobCategory category, ChunkPos pos, CustomMobSpawningRules rules) {
		return this.counts.getOrDefault(category, 0) < rules.getChunkMobCapFor(category);
	}
}
