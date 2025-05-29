/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.ducks;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface ArcadeTeamDataHolder {
    @Nullable CompoundTag arcade$getData();

    void arcade$setData(@Nullable CompoundTag tag);
}
