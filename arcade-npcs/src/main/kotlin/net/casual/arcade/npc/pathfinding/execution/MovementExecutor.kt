/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.pathfinding.execution

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ai.NPCInput
import net.casual.arcade.npc.pathfinding.movement.Movement

/**
 * Performs a single [Movement] by driving a player's inputs.
 *
 * An executor is created when its movement becomes the current one and discarded when it
 * finishes, so it may hold whatever state it needs across ticks.
 *
 * @see Movement.createExecutor
 * @see MovementControls
 */
public interface MovementExecutor {
    /**
     * Called on the first tick this movement is the current one.
     *
     * @param player The player performing the movement.
     */
    public fun start(player: FakePlayer) {

    }

    /**
     * Drives [input] for one tick.
     *
     * @param player The player performing the movement.
     * @param input The input to write to, already reset for this tick.
     * @return Whether the movement is still going, finished, or has failed.
     */
    public fun tick(player: FakePlayer, input: NPCInput): MovementStatus

    /**
     * Called when this movement stops being the current one.
     *
     * @param player The player that was performing the movement.
     */
    public fun stop(player: FakePlayer) {

    }
}
