/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.render

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.Event
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

public data class GuiRenderEvent(
    val graphics: GuiGraphics,
    val deltas: DeltaTracker
): Event {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}