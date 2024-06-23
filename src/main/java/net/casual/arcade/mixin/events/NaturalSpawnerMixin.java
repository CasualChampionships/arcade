package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.entity.MobCategorySpawnEvent;
import net.casual.arcade.events.entity.MobSpawnEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@WrapWithCondition(
		method = "spawnForChunk",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/NaturalSpawner;spawnCategoryForChunk(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"
		)
	)
	private static boolean canMobCategorySpawn(
		MobCategory category,
		ServerLevel level,
		LevelChunk chunk,
		NaturalSpawner.SpawnPredicate filter,
		NaturalSpawner.AfterSpawnCallback callback,
		ServerLevel foo,
		LevelChunk bar,
		NaturalSpawner.SpawnState state
	) {
		MobCategorySpawnEvent event = new MobCategorySpawnEvent(level, category, chunk.getPos(), state);
		GlobalEventHandler.broadcast(event);
		return !event.isCancelled();
	}

	@ModifyExpressionValue(
		method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/NaturalSpawner;isValidPositionForMob(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;D)Z"
		)
	)
	private static boolean canMobSpawn(boolean original, @Local(argsOnly = true) ServerLevel level, @Local Mob mob, @Local SpawnGroupData data) {
		if (!original) {
			return false;
		}
		MobSpawnEvent event = new MobSpawnEvent(level, mob, data);
		GlobalEventHandler.broadcast(event);
		return !event.isCancelled();
	}
}
