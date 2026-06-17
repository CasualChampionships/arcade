/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ServerLevel.class)
public interface ServerLevelAccessor {
    @Accessor("ENTITY_COUNTER")
    static AtomicInteger accessEntityCounter() {
        throw new AssertionError();
    }
}
