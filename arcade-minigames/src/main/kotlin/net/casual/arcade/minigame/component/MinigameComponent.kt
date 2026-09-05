/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.scope.MinigameScope

/**
 * A modular component which can be dynamically added
 * or removed from any [Minigame] instance via
 * [MinigameComponents.add]/[MinigameComponents.remove].
 *
 * Each [MinigameComponent] requires a respective
 * [MinigameComponentType] which is used to query the
 * [MinigameComponent] via [MinigameComponents.get].
 *
 * Components can be serialized, but require implementing
 * the [SerializableMinigameComponent].
 *
 * @see MinigameComponents
 * @see SerializableMinigameComponent
 */
public interface MinigameComponent {
    /**
     * This is where any of the [MinigameComponent]s logic
     * should be initialized. Events and tasks should be
     * registered against [scope], so that if this
     * [MinigameComponent] is removed later everything
     * is properly cleaned up.
     *
     * @param scope The [MinigameScope] that this component
     *   is tied to.
     * @see MinigameScope
     */
    public fun initialize(scope: MinigameScope) {

    }

    /**
     * This is called when the [MinigameComponent] is closed.
     * This can be either when the owning minigame is closed
     * *or* if this component is removed.
     */
    public fun close() {

    }

    /**
     * Gets the type of the minigame component.
     *
     * @return The [MinigameComponentType].
     * @see MinigameComponentType
     */
    public fun type(): MinigameComponentType<*>
}
