/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import net.minecraft.network.chat.Style

public fun interface WidthResolver {
    public fun width(codepoint: Int, style: Style): Int
}