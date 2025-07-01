/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Set;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
	@Shadow @Final private ServerLevelData serverLevelData;

	@Shadow @Final private MinecraftServer server;

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

	@ModifyReturnValue(
		method = "tickRateManager",
		at = @At("RETURN")
	)
	private TickRateManager getTickRateManager(TickRateManager original) {
		Set<Minigame> minigames = MinigameUtils.getMinigames((ServerLevel) (Object) this);
		if (minigames.isEmpty()) {
			return original;
		}
		return minigames.iterator().next().getTickrate();
	}

	@ModifyArg(
		method = "tickTime",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;setDayTime(J)V"
		)
	)
	private long onSetDayTime(long time) {
		Set<Minigame> minigames = MinigameUtils.getMinigames((ServerLevel) (Object) this);
		if (minigames.size() != 1) {
			return time;
		}
		Minigame minigame = minigames.iterator().next();
		int speed = minigame.getSettings().getDaylightCycle();
		long newTime = this.serverLevelData.getDayTime() + speed;
		if (speed > 1 && this.server.getTickCount() % 20 != 0) {
			Packet<?> packet = new ClientboundSetTimePacket(
				this.serverLevelData.getGameTime(), newTime, true
			);
			for (ServerPlayer player : minigame.getPlayers()) {
				if (player.level().dimension() == this.dimension()) {
					player.connection.send(packet);
				}
			}
		}
		return newTime;
	}
}
