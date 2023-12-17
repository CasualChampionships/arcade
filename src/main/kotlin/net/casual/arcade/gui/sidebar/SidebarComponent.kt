package net.casual.arcade.gui.sidebar

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.numbers.NumberFormat

public data class SidebarComponent(
    val display: Component,
    val score: NumberFormat? = null
)
