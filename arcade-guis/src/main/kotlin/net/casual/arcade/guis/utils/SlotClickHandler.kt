/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils

public fun interface SlotClickHandler {
    /**
     * Handles a slot click action.
     *
     * @param action The action that was performed.
     */
    public fun invoke(action: SlotClickAction)
}