/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.ducks;

import org.jetbrains.annotations.Nullable;

public interface OverridableColor {
	void arcade$setColor(@Nullable Integer color);

	@Nullable Integer arcade$getColor();
}
