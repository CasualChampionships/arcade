package net.casualuhc.arcade.events.border

import net.casualuhc.arcade.border.CustomBorder
import net.casualuhc.arcade.events.core.Event

class CustomBorderFinishEvent(
    val border: CustomBorder,
    val forced: Boolean
): Event()