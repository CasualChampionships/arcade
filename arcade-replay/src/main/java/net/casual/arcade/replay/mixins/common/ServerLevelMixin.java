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
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private void onDestroyBlockProgress(int id, BlockPos blockPos, int progress, CallbackInfo ci) {
        Entity breaker = this.getEntity(id);
        if (breaker instanceof ServerPlayer player) {
			ReplayPlayerRecorders.record(player, new ClientboundBlockDestructionPacket(id, blockPos, progress));
        }

        ChunkPos chunkPos = ChunkPos.containing(blockPos);
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(this.dimension(), chunkPos)) {
            recorder.record(new ClientboundBlockDestructionPacket(id, blockPos, progress));
        }
    }

    @Inject(
        method = "explode",
        at = @At("TAIL")
    )
    private void onExplode(
        @Nullable Entity source,
        @Nullable DamageSource damageSource,
        @Nullable ExplosionDamageCalculator damageCalculator,
        double x,
        double y,
        double z,
        float r,
        boolean fire,
        ExplosionInteraction interactionType,
        ParticleOptions smallExplosionParticles,
        ParticleOptions largeExplosionParticles,
        WeightedList<ExplosionParticleInfo> blockParticles,
        Holder<SoundEvent> explosionSound,
        CallbackInfo ci,
        @Local(name = "center") Vec3 center,
        @Local(name = "explosionParticle") ParticleOptions explosionParticle,
        @Local(name = "blockCount") int blockCount
    ) {
        ChunkPos chunkPos = ChunkPos.containing(BlockPos.containing(x, y, z));
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(this.dimension(), chunkPos)) {
            recorder.record(new ClientboundExplodePacket(center, r, blockCount, Optional.empty(), explosionParticle, explosionSound, blockParticles));
        }
    }

    @Inject(
        method = "sendParticles(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDIDDDD)I",
        at = @At("TAIL")
    )
    private <T extends ParticleOptions> void onSendParticles(
        T particle,
        boolean overrideLimiter,
        boolean alwaysShow,
        double x,
        double y,
        double z,
        int count,
        double xDist,
        double yDist,
        double zDist,
        double speed,
        CallbackInfoReturnable<Integer> cir,
        @Local(name = "packet") ClientboundLevelParticlesPacket packet
    ) {
        ChunkPos chunkPos = ChunkPos.containing(BlockPos.containing(x, y, z));
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(this.dimension(), chunkPos)) {
            recorder.record(packet);
        }
    }
}
