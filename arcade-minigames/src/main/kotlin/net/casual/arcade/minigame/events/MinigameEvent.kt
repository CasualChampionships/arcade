/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.events.common.Event
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameEventHandler

/**
 * Superclass for all minigame-related events.
 *
 * This is to be able to filter out [MinigameEvent]'s
 * in [Minigame]s, allowing you to only listen to
 * [MinigameEvent]s for the levels in the given [Minigame].
 *
 * @see MinigameEventHandler.register
 */
public interface MinigameEvent: Event {
    /**
     * The [Minigame] that is tied to the event.
     */
    public val minigame: Minigame
}