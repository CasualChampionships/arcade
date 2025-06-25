/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.resources.ResourceLocation
import java.util.*

public object ResourceUtils {
    // @Deprecated("Use ArcadeUtils instead", ReplaceWith("ArcadeUtils.id(path)"))
    public fun arcade(path: String): ResourceLocation {
        return ArcadeUtils.id(path)
    }

    public fun random(): ResourceLocation {
        val key = UUID.randomUUID().toString()
        return ResourceLocation.fromNamespaceAndPath(ArcadeUtils.MOD_ID, key)
    }

    public fun random(modifier: (String) -> String): ResourceLocation {
        val key = UUID.randomUUID().toString()
        return ResourceLocation.fromNamespaceAndPath(ArcadeUtils.MOD_ID, modifier(key))
    }
}
