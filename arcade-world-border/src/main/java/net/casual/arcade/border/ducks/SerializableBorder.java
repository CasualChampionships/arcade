/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.ducks;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface SerializableBorder {
	void arcade$serialize(ValueOutput output);

	void arcade$deserialize(ValueInput compound);
}
