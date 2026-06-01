/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

public fun interface SlotInteractHandler {
    /**
     * Handles some slot interaction with the world.
     *
     * @param action The action that was performed.
     * @return Whether the interaction was consumed.
     */
    public fun invoke(action: SlotInteractAction): Boolean
}