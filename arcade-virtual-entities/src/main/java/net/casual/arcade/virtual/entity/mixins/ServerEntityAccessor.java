/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerEntity.class)
public interface ServerEntityAccessor {
    @Accessor("TOLERANCE_LEVEL_POSITION")
    static double accessToleranceLevelPosition() {
        throw new AssertionError();
    }

    @Accessor("TOLERANCE_LEVEL_ROTATION")
    static int accessToleranceLevelRotation() {
        throw new AssertionError();
    }
}
