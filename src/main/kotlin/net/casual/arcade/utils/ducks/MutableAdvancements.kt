package net.casual.arcade.utils.ducks

import net.minecraft.advancements.Advancement

internal interface MutableAdvancements {
    fun addAdvancement(advancement: Advancement)

    fun removeAdvancement(advancement: Advancement)
}