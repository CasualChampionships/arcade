/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.entity.MobSpawnEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@ModifyExpressionValue(
		method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/NaturalSpawner;isValidPositionForMob(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;D)Z"
		)
	)
	private static boolean canMobSpawn(
		boolean original,
		@Local(name = "level", argsOnly = true) ServerLevel level,
		@Local(name = "mob") Mob mob,
		@Local(name = "groupData") SpawnGroupData groupData
	) {
		if (!original) {
			return false;
		}
		MobSpawnEvent event = new MobSpawnEvent(level, mob, groupData);
		GlobalEventHandler.Server.broadcast(event);
		return !event.isCancelled();
	}
}
