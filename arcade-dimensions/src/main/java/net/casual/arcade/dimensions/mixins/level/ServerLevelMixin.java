/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.level.CustomLevel;
import net.casual.arcade.dimensions.level.LevelGenerationOptions;
import net.casual.arcade.dimensions.utils.impl.DerivedLevelData;
import net.casual.arcade.util.ducks.SpoofedDimensionKeyHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements SpoofedDimensionKeyHolder {
	@Unique private ResourceKey<Level> arcade_spoofedKey = null;

	@Shadow @Final private ServerLevelData serverLevelData;

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

	@ModifyExpressionValue(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/WorldGenSettings;options()Lnet/minecraft/world/level/levelgen/WorldOptions;"
		)
	)
	private WorldOptions modifyWorldOptions(WorldOptions original, MinecraftServer server) {
        Optional<DerivedLevelData> optional = this.getCustomLevelData();
		if (optional.isPresent()) {
			LevelGenerationOptions options = optional.get().getOptions();
			return new WorldOptions(options.getSeed(), options.getGenerateStructures(), original.generateBonusChest());
		}
		return original;
	}

    @ModifyExpressionValue(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;getViewDistance()I"
        )
    )
    private int getLevelViewDistance(int original) {
        return this.getCustomLevelData()
            .flatMap(data -> data.getProperties().getViewDistance())
            .orElse(original);
    }

    @ModifyExpressionValue(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;getSimulationDistance()I"
        )
    )
    private int getLevelSimulationDistance(int original) {
        return this.getCustomLevelData()
            .flatMap(data -> data.getProperties().getSimulationDistance())
            .orElse(original);
    }

	@Redirect(
		method = "advanceWeatherCycle",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
		)
	)
	private void onSendWeatherPackets(PlayerList instance, Packet<?> packet) {
		instance.broadcastAll(packet, this.dimension());
	}

	@Override
	public void arcade_setSpoofedDimensionKey(ResourceKey<Level> key) {
		this.arcade_spoofedKey = key;
	}

	@Nullable
	@Override
	public ResourceKey<Level> arcade_getSpoofedDimensionKey() {
		return this.arcade_spoofedKey;
	}

    @Unique
    private Optional<DerivedLevelData> getCustomLevelData() {
        if ((Object) this instanceof CustomLevel) {
            return Optional.of(((DerivedLevelData) this.serverLevelData));
        }
        return Optional.empty();
    }
}
