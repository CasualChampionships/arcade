/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.level;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.dimensions.ArcadeDimensions;
import net.casual.arcade.dimensions.level.CustomLevel;
import net.casual.arcade.dimensions.utils.LevelPersistenceTracker;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow public abstract Iterable<ServerLevel> getAllLevels();

	@ModifyExpressionValue(
		method = "tickChildren",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;getAllLevels()Ljava/lang/Iterable;"
		)
	)
	private Iterable<ServerLevel> copyLevelIterable(Iterable<ServerLevel> original) {
		// We do this in the case that someone adds a level
		// during the world tick phase which would normally throw a CME
		return Lists.newArrayList(original);
	}

	@Inject(
		method = "createLevels",
		at = @At("TAIL")
	)
	private void afterCreateVanillaLevels(CallbackInfo ci) {
		MinecraftServer server = (MinecraftServer) (Object) this;
		for (ResourceKey<Level> key : LevelPersistenceTracker.loadPersistentLevels(server)) {
			ArcadeDimensions.load(server, key);
		}
	}

	@Inject(
		method = "stopServer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;saveAllChunks(ZZZ)Z"
		)
	)
	private void beforeSaveAllChunks(CallbackInfo ci) {
		for (ServerLevel level : Lists.newArrayList(this.getAllLevels())) {
			if (level instanceof CustomLevel custom && !custom.getPersistence().shouldSave()) {
				ArcadeDimensions.delete((MinecraftServer) (Object) this, custom);
			}
		}
	}
}
