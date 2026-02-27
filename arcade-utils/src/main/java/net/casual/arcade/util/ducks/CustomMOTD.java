/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.ducks;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public interface CustomMOTD {
	void arcade_setMOTD(Component message);

	@Nullable Component arcade_getMOTD();
}
