package net.casual.arcade.dimensions.mixins.vanilla;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel;
import net.casual.arcade.dimensions.level.vanilla.extension.DragonDataExtension;
import net.casual.arcade.extensions.event.LevelExtensionEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
	@Shadow @Nullable private EndDragonFight dragonFight;

	@Shadow public abstract long getSeed();

	protected ServerLevelMixin(
		WritableLevelData levelData,
		ResourceKey<Level> dimension,
		RegistryAccess registryAccess,
		Holder<DimensionType> dimensionTypeRegistration,
		boolean isClientSide,
		boolean isDebug,
		long biomeZoomSeed,
		int maxChainedNeighborUpdates
	) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onInitServerLevel(
		CallbackInfo ci,
		@Local(argsOnly = true) MinecraftServer server
	) {
		// We don't load dragon data for the end, we let vanilla handle this.
		if (this.dimension() == Level.END) {
			return;
		}
		// We need to do this later because we haven't initialized our world fully
		server.schedule(new TickTask(server.getTickCount(), () -> {
			if (this.dragonFight == null && VanillaLikeLevel.getLikeDimension(this) == Level.END && this.dimensionTypeRegistration().is(BuiltinDimensionTypes.END)) {
				ServerLevel level = (ServerLevel) (Object) this;
				// We use an extension here because dragon data by default is stored for the entire server. We need it per level
				DragonDataExtension extension = LevelExtensionEvent.getExtension(level, DragonDataExtension.class);
				this.dragonFight = new EndDragonFight(level, this.getSeed(), extension.getDataOrDefault());
			}
		}));
	}
}
