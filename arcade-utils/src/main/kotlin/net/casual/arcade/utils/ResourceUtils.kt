package net.casual.arcade.utils

import net.minecraft.resources.ResourceLocation
import org.apache.commons.lang3.RandomStringUtils

public object ResourceUtils {
    public fun arcade(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(ArcadeUtils.MOD_ID, path)
    }

    public fun random(): ResourceLocation {
        val key = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789")
        return ResourceLocation.fromNamespaceAndPath(ArcadeUtils.MOD_ID, key)
    }

    public fun random(modifier: (String) -> String): ResourceLocation {
        val key = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789")
        return ResourceLocation.fromNamespaceAndPath(ArcadeUtils.MOD_ID, modifier(key))
    }
}
