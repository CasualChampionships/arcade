/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.annotation

import net.casual.arcade.events.server.level.LevelEvent
import net.casual.arcade.events.server.level.LocatedLevelEvent
import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.minigame.events.MinigameEvent
import net.casual.arcade.minigame.managers.MinigameLevelManager
import net.casual.arcade.minigame.managers.MinigamePlayerManager
import java.util.*

/**
 * This enum describes filters which can be applied
 * to minigame event listeners.
 */
public enum class ListenerFilter {
    /**
     * This filters all [PlayerEvent]s so that only
     * events with [PlayerEvent.player] that are in
     * the minigame will trigger.
     */
    HasPlayer,

    /**
     * This filters all [PlayerEvent]s so that only
     * events with [PlayerEvent.player] that are
     * in [MinigamePlayerManager.playing] will trigger.
     */
    IsPlaying,

    /**
     * This filters all [PlayerEvent]s so that only
     * events with [PlayerEvent.player] that are
     * in [MinigamePlayerManager.spectating] will trigger.
     */
    IsSpectator,

    /**
     * This filters all [PlayerEvent]s so that only
     * events with [PlayerEvent.player] that are
     * in [MinigamePlayerManager.admins] will trigger.
     */
    IsAdmin,

    /**
     * This filters all [LevelEvent]s so that only
     * events with [LevelEvent.level] that are in
     * [MinigameLevelManager.levels] will trigger.
     */
    HasLevel,

    /**
     * This filters all [LocatedLevelEvent]s so that only
     * events with [LocatedLevelEvent.level] that are
     * in [MinigameLevelManager.levels] *and* that have
     * [LocatedLevelEvent.pos] in bounds will trigger.
     */
    InLevelBounds,

    /**
     * This filters all [MinigameEvent]s so that only
     * events with [MinigameEvent.minigame] will trigger
     * for that specific minigame.
     */
    IsMinigame;

    public companion object {
        private val DEFAULT = of(HasPlayer, HasLevel, InLevelBounds, IsMinigame)

        /**
         * Ths default set of listener filters is designed so that
         * minigames listen to only what is relevant to it.
         *
         * This includes [HasPlayer], [HasLevel], [InLevelBounds], and [IsMinigame].
         *
         * @return The default set of filters.
         */
        @JvmStatic
        public fun default(): Set<ListenerFilter> {
            return DEFAULT
        }

        /**
         * This is the empty set of filters and will allow
         * all events to be listened to.
         *
         * @return The empty filter set.
         */
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
