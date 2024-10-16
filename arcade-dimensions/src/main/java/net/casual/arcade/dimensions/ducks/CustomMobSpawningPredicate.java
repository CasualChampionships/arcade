package net.casual.arcade.dimensions.ducks;

import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;

public interface CustomMobSpawningPredicate {
	boolean arcade$canSpawn(MobCategory category, ChunkPos pos, CustomMobSpawningRules rules);
}
