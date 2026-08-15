/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.utils

import net.casual.arcade.utils.arcade
import net.minecraft.resources.Identifier

public object BuiltInFonts {
    public val MINI_FONT: Identifier = arcade("mini_minecraft")

    public fun shiftedDownFont(shift: Int): Identifier {
        return arcade("default_shifted_down_$shift")
    }

    public fun miniShiftedDownFont(shift: Int): Identifier {
        return arcade("mini_shifted_down_$shift")
    }
}