package net.casual.arcade.utils.ducks

import net.minecraft.advancements.AdvancementHolder

internal interface MutableAdvancements {
    fun addAllAdvancements(advancements: Collection<AdvancementHolder>)

    fun addAdvancement(advancement: AdvancementHolder)

    fun removeAdvancement(advancement: AdvancementHolder)
}