package net.casual.arcade.utils

import net.casual.arcade.ducks.`Arcade$MutableAdvancements`
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.server.ServerAdvancementManager

public object AdvancementUtils {
    public fun ServerAdvancementManager.addAllAdvancements(advancements: Collection<AdvancementHolder>) {
        (this as `Arcade$MutableAdvancements`).`arcade$addAllAdvancements`(advancements)
    }

    public fun ServerAdvancementManager.addAdvancement(advancement: AdvancementHolder) {
        (this as `Arcade$MutableAdvancements`).`arcade$addAdvancement`(advancement)
    }

    public fun ServerAdvancementManager.removeAdvancement(advancement: AdvancementHolder) {
        (this as `Arcade$MutableAdvancements`).`arcade$removeAdvancement`(advancement)
    }
}