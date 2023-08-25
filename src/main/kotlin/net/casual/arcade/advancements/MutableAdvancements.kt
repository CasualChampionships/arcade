package net.casual.arcade.advancements

import net.minecraft.advancements.Advancement

interface MutableAdvancements {
    fun addAdvancement(advancement: Advancement)

    fun removeAdvancement(advancement: Advancement)
}