package net.casual.arcade.utils

import net.casual.arcade.utils.ducks.MutableAdvancements
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.server.ServerAdvancementManager

public object AdvancementUtils {
    public fun ServerAdvancementManager.addAllAdvancements(advancements: Collection<AdvancementHolder>) {
        (this as MutableAdvancements).addAllAdvancements(advancements)
    }

    public fun ServerAdvancementManager.addAdvancement(advancement: AdvancementHolder) {
        (this as MutableAdvancements).addAdvancement(advancement)
    }

    public fun ServerAdvancementManager.removeAdvancement(advancement: AdvancementHolder) {
        (this as MutableAdvancements).removeAdvancement(advancement)
    }
}