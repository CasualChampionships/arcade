/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.casual.arcade.minigame.scope.MinigameScope

public interface MinigameComponent {
    public fun initialize(scope: MinigameScope) {

    }

    public fun close() {

    }

    public fun type(): MinigameComponentType<*>
}
