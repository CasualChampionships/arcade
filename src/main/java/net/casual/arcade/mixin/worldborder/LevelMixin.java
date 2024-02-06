package net.casual.arcade.mixin.worldborder;

import net.casual.arcade.border.TrackedBorder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(Level.class)
public class LevelMixin implements Arcade$MutableWorldBorder {
	@Shadow @Mutable @Final private WorldBorder worldBorder;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateLevel(
		WritableLevelData data,
		ResourceKey<Level> dimension,
		RegistryAccess access,
		Holder<DimensionType> holder,
		Supplier<ProfilerFiller> profiler,
		boolean client,
		boolean debug,
		long biome,
		int maxChain,
		CallbackInfo ci
	) {
		this.worldBorder = new TrackedBorder(29999984, 0, 0);
	}

	@Override
	public void arcade$setWorldBorder(WorldBorder border) {
		this.worldBorder = border;
	}
}
