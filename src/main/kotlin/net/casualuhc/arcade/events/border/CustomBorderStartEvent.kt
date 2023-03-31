package net.casualuhc.arcade.events.border

import net.casualuhc.arcade.border.CustomBorder
import net.casualuhc.arcade.events.core.Event

class CustomBorderStartEvent(
    val border: CustomBorder,
    val start: Double,
    val end: Double,
    val duration: Long
): Event()