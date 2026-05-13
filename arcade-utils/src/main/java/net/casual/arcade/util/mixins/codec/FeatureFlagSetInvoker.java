/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.codec;

import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FeatureFlagSet.class)
public interface FeatureFlagSetInvoker {
    @Invoker("<init>")
    static FeatureFlagSet arcade_init(@Nullable final FeatureFlagUniverse universe, final long mask) {
        throw new AssertionError();
    }
}
