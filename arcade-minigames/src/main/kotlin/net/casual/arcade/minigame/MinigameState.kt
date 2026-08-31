/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame

import net.casual.arcade.minigame.phase.Phase

public sealed interface MinigameState {
    public fun isAt(phase: Phase<*>): Boolean {
        return (this as? Playing)?.phase == phase
    }

    public operator fun compareTo(phase: Phase<*>): Int {
        val current = (this as? Playing)?.phase ?: return -1
        return current.compareTo(phase)
    }

    public data object Created: MinigameState

    public data object Ready: MinigameState

    public data class Playing(public val phase: Phase<Minigame>): MinigameState

    public data class Closed(public val completed: Boolean): MinigameState
}
