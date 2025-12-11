/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.flashback

import net.casual.arcade.utils.Identifier
import net.minecraft.resources.Identifier

public object FlashbackUtils {
    public const val MOD_ID: String = "flashback"

    public fun id(path: String): Identifier {
        return Identifier(MOD_ID, path)
    }
}