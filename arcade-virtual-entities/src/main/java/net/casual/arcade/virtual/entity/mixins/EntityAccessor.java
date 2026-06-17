/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor("DATA_SHARED_FLAGS_ID")
    static EntityDataAccessor<Byte> accessSharedFlagsAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_AIR_SUPPLY_ID")
    static EntityDataAccessor<Integer> accessAirSupplyAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_CUSTOM_NAME")
    static EntityDataAccessor<Optional<Component>> accessCustomNameAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_CUSTOM_NAME_VISIBLE")
    static EntityDataAccessor<Boolean> accessCustomNameVisibleAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_SILENT")
    static EntityDataAccessor<Boolean> accessSilentAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_NO_GRAVITY")
    static EntityDataAccessor<Boolean> accessNoGravityAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_POSE")
    static EntityDataAccessor<Pose> accessPoseAccessor() {
        throw new AssertionError();
    }

    @Accessor("DATA_TICKS_FROZEN")
    static EntityDataAccessor<Integer> accessTicksFrozenAccessor() {
        throw new AssertionError();
    }

    @Accessor("FLAG_ONFIRE")
    static int accessOnFireFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_SHIFT_KEY_DOWN")
    static int accessShiftKeyDownFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_SPRINTING")
    static int accessSprintingFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_SWIMMING")
    static int accessSwimmingFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_INVISIBLE")
    static int accessInvisibleFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_GLOWING")
    static int accessGlowingFlag() {
        throw new AssertionError();
    }

    @Accessor("FLAG_FALL_FLYING")
    static int accessFallFlyingFlag() {
        throw new AssertionError();
    }
}
