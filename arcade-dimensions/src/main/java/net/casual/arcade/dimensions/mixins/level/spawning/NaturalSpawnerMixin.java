package net.casual.arcade.dimensions.mixins.level.spawning;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules;
import net.casual.arcade.dimensions.level.spawner.extension.LevelCustomMobSpawningExtension;
import net.casual.arcade.extensions.event.LevelExtensionEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@WrapOperation(
		method = "spawnForChunk",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"
		)
	)
	private static boolean canCategorySpawn(
		NaturalSpawner.SpawnState instance,
		MobCategory category,
		ChunkPos pos,
		Operation<Boolean> original,
		ServerLevel level
	) {
		LevelCustomMobSpawningExtension extension = LevelExtensionEvent.getExtension(level, LevelCustomMobSpawningExtension.class);
		CustomMobSpawningRules rules = extension.getRules();
		if (rules == null) {
			return original.call(instance, category, pos);
		}
		int currentCategoryCount = instance.getMobCategoryCounts().getInt(category);
		int spawnableChunks =  instance.getSpawnableChunkCount();
		LocalMobCapCalculator calculator = ((SpawnStateAccessor) instance).getLocalMobCapCalculator();
		return rules.canCategorySpawn(category, pos, currentCategoryCount, spawnableChunks, calculator);
	}
}
