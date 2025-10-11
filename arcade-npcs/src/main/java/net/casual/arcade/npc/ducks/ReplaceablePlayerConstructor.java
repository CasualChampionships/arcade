/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.ducks;

import net.casual.arcade.npc.configuration.FakePlayerConstructor;

public interface ReplaceablePlayerConstructor {
    void arcade$set(FakePlayerConstructor<?> constructor);
}
