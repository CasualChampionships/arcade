/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.annotation

import java.util.*

public enum class ListenerFilter {
    HasPlayer,
    IsPlaying,
    IsSpectator,
    IsAdmin,
    HasLevel,
    InLevelBounds,
    IsMinigame;

    public companion object {
        private val DEFAULT = of(HasPlayer, HasLevel, InLevelBounds, IsMinigame)

        @JvmStatic
        public fun default(): Set<ListenerFilter> {
            return DEFAULT
        }

        @JvmStatic
        public fun unfiltered(): Set<ListenerFilter> {
            return Collections.emptySet()
        }

        @JvmStatic
        public fun of(vararg filters: ListenerFilter): Set<ListenerFilter> {
            if (filters.isEmpty()) {
                return this.unfiltered()
            }
            return EnumSet.of(filters[0], *filters)
        }
    }
}
