/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.Optional;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("DATA_LIVING_ENTITY_FLAGS")
    static EntityDataAccessor<Byte> accessLivingEntityFlagsAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> accessHealthAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_EFFECT_PARTICLES")
    static EntityDataAccessor<List<ParticleOptions>> accessEffectParticlesAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_EFFECT_AMBIENCE_ID")
    static EntityDataAccessor<Boolean> accessEffectAmbienceAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_ARROW_COUNT_ID")
    static EntityDataAccessor<Integer> accessArrowCountAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_STINGER_COUNT_ID")
    static EntityDataAccessor<Integer> accessStingerCountAccessor() {
        throw new AssertionError();
    }

    @Accessor("SLEEPING_POS_ID")
    static EntityDataAccessor<Optional<BlockPos>> accessSleepingPosAccessor() {
        throw new AssertionError();
    }

    @Accessor("LIVING_ENTITY_FLAG_IS_USING")
    static int accessIsUsingFlag() {
        throw new AssertionError();
    }

    @Accessor("LIVING_ENTITY_FLAG_OFF_HAND")
    static int accessOffHandFlag() {
        throw new AssertionError();
    }

    @Accessor("LIVING_ENTITY_FLAG_SPIN_ATTACK")
    static int accessSpinAttackFlag() {
        throw new AssertionError();
    }
}
