/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.ducks;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface SpoofedDimensionKeyHolder {
    void arcade$setSpoofedDimensionKey(@Nullable ResourceKey<Level> key);

    @Nullable ResourceKey<Level> arcade$getSpoofedDimensionKey();
}
