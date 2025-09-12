/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.flashback

import net.casual.arcade.utils.ResourceLocation
import net.minecraft.resources.ResourceLocation

public object FlashbackUtils {
    public const val MOD_ID: String = "flashback"

    public fun id(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }
}