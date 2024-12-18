/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.ducks;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface SerializableBorder {
	CompoundTag arcade$serialize();

	void arcade$deserialize(@NotNull CompoundTag compound);
}
