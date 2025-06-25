/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.level;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.event.LevelExtensionEvent;
import net.casual.arcade.utils.ArcadeUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ExtensionHolder {
	@Unique private final ExtensionMap arcade$extensionMap = new ExtensionMap();
	@Unique private Path arcade$savePath;

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
	private void onCreateLevel(
		CallbackInfo ci,
		@Local(argsOnly = true) LevelStorageSource.LevelStorageAccess access
	) {
		this.arcade$savePath = access.getDimensionPath(this.dimension()).resolve("arcade-extension-data.nbt");

		LevelExtensionEvent event = new LevelExtensionEvent((ServerLevel) (Object) this);
		GlobalEventHandler.Server.broadcast(event);

		try (ProblemReporter.ScopedCollector collector = ArcadeUtils.createProblemReporter()) {
			CompoundTag tag = Optional.ofNullable(NbtIo.read(this.arcade$savePath)).orElseGet(CompoundTag::new);
			ValueInput input = TagValueInput.create(collector, this.registryAccess(), tag);
			ExtensionHolder.deserialize(this, input);
		} catch (IOException e) {
			ArcadeUtils.logger.error("Failed to read arcade extension data", e);
		}
	}

	@Inject(
		method = "saveLevelData",
		at = @At("TAIL")
	)
	private void onSaveLevel(CallbackInfo ci) {
		try (ProblemReporter.ScopedCollector collector = ArcadeUtils.createProblemReporter()) {
			TagValueOutput output =  TagValueOutput.createWithContext(collector, this.registryAccess());
			ExtensionHolder.serialize(this, output);
			Files.createDirectories(this.arcade$savePath.getParent());
			NbtIo.write(output.buildResult(), this.arcade$savePath);
		} catch (IOException e) {
			ArcadeUtils.logger.error("Failed to save arcade extension data", e);
		}
	}

	@NotNull
	@Override
	@SuppressWarnings("AddedMixinMembersNamePattern")
	public ExtensionMap getExtensionMap() {
		return this.arcade$extensionMap;
	}
}
