package net.casual.arcade.utils

import net.casual.arcade.utils.ducks.MutableAdvancements
import net.minecraft.advancements.Advancement
import net.minecraft.server.ServerAdvancementManager

public object AdvancementUtils {
    public fun ServerAdvancementManager.addAdvancement(advancement: Advancement) {
        (this as MutableAdvancements).addAdvancement(advancement)
    }

    public fun ServerAdvancementManager.removeAdvancement(advancement: Advancement) {
        (this as MutableAdvancements).removeAdvancement(advancement)
    }
}