package net.casual.arcade.utils

import net.minecraft.resources.ResourceLocation
import java.util.*

public object ResourceUtils {
    public fun arcade(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(ArcadeUtils.MOD_ID, path)
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
