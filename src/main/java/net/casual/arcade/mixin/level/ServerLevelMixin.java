package net.casual.arcade.mixin.level;

import net.casual.arcade.scheduler.GlobalTickedScheduler;
import net.casual.arcade.utils.LevelUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
	@Shadow @Nullable private EndDragonFight dragonFight;
	@Shadow @Final private MinecraftServer server;

	protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onInitServerLevel(
		MinecraftServer server,
		Executor dispatcher,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		ServerLevelData serverLevelData,
		ResourceKey<Level> dimension,
		LevelStem levelStem,
		ChunkProgressListener progressListener,
		boolean isDebug,
		long biomeZoomSeed,
		List<CustomSpawner> customSpawners,
		boolean tickTime,
		RandomSequences randomSequences,
		CallbackInfo ci
	) {
		// We need to do this later because we haven't initialized our world fully
		GlobalTickedScheduler.later(() -> {
			if (this.dragonFight == null && LevelUtils.getLikeDimension(this) == Level.END && this.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
				WorldData data = this.server.getWorldData();
				this.dragonFight = new EndDragonFight((ServerLevel) (Object) this, data.worldGenOptions().seed(), data.endDragonFightData());
			}
		});
	}
}
