/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.ducks;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ExtensionDataHolder {
	void arcade$deserializeExtensionData();
}
