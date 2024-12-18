/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.level.spawning;

import net.casual.arcade.dimensions.ducks.CustomMobSpawningPredicate;
import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(LocalMobCapCalculator.class)
public abstract class LocalMobCapCalculatorMixin implements CustomMobSpawningPredicate {
	@Shadow @Final private Map<ServerPlayer, Object> playerMobCounts;

	@Shadow protected abstract List<ServerPlayer> getPlayersNear(ChunkPos pos);

	@Override
	public boolean arcade$canSpawn(MobCategory category, ChunkPos pos, CustomMobSpawningRules rules) {
		for (ServerPlayer serverPlayer : this.getPlayersNear(pos)) {
			Object mobCounts = this.playerMobCounts.get(serverPlayer);
			if (mobCounts == null || ((CustomMobSpawningPredicate) mobCounts).arcade$canSpawn(category, pos, rules)) {
				return true;
			}
		}
		return false;
	}
}
