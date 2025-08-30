/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.common;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
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

    @Shadow
    @Nullable
    public abstract Entity getEntity(int id);

	@Inject(
        method = "destroyBlockProgress",
        at = @At("TAIL")
    )
    private void onDestroyBlockProgress(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        Entity breaker = this.getEntity(breakerId);
        if (breaker instanceof ServerPlayer player) {
			ReplayPlayerRecorders.record(player, new ClientboundBlockDestructionPacket(breakerId, pos, progress));
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(this.dimension(), chunkPos)) {
            recorder.record(new ClientboundBlockDestructionPacket(breakerId, pos, progress));
        }
    }

    @Inject(
        method = "explode",
        at = @At("TAIL")
    )
    private void onExplode(
        @Nullable Entity entity,
        @Nullable DamageSource source,
        @Nullable ExplosionDamageCalculator calculator,
        double posX,
        double posY,
        double posZ,
        float radius,
        boolean causeFire,
        ExplosionInteraction interaction,
        ParticleOptions smallParticles,
        ParticleOptions largeParticles,
        Holder<SoundEvent> sound,
        CallbackInfo ci,
        @Local Vec3 pos,
        @Local(ordinal = 2) ParticleOptions particles
    ) {
        ChunkPos chunkPos = new ChunkPos(BlockPos.containing(posX, posY, posZ));
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(this.dimension(), chunkPos)) {
            recorder.record(new ClientboundExplodePacket(pos, Optional.empty(), particles, sound));
        }
    }

    @Inject(
        method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDIDDDD)I",
        at = @At("TAIL")
    )
    private <T extends ParticleOptions> void onSendParticles(
        T type,
        boolean overrideLimiter,
        boolean alwaysShow,
        double posX,
        double posY,
        double posZ,
        int particleCount,
        double xOffset,
        double yOffset,
        double zOffset,
        double speed,
        CallbackInfoReturnable<Integer> cir,
        @Local ClientboundLevelParticlesPacket packet
    ) {
        ChunkPos chunkPos = new ChunkPos(BlockPos.containing(posX, posY, posZ));
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(this.dimension(), chunkPos)) {
            recorder.record(packet);
        }
    }
}
