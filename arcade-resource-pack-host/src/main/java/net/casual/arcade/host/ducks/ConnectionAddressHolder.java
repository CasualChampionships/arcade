/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.ducks;

import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import org.jetbrains.annotations.Nullable;

public interface ConnectionAddressHolder {
    @Nullable ObjectIntPair<String> arcade$getConnectionAddress();
}
