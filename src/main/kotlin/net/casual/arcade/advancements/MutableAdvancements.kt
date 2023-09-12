package net.casual.arcade.advancements

import net.minecraft.advancements.Advancement

@Suppress("FunctionName")
internal interface MutableAdvancements {
    fun `arcade$addAdvancement`(advancement: Advancement)

    fun `arcade$removeAdvancement`(advancement: Advancement)
}