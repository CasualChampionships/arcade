/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.codec;

import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FeatureFlagRegistry.class)
public interface FeatureFlagRegistryAccessor {
    @Accessor("universe")
    FeatureFlagUniverse arcade_getUniverse();
}
