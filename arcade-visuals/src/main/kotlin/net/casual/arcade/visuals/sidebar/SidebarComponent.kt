/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.sidebar

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.numbers.BlankFormat
import net.minecraft.network.chat.numbers.FixedFormat
import net.minecraft.network.chat.numbers.NumberFormat

public data class SidebarComponent(
    val display: Component? = null,
    val score: NumberFormat? = null
) {
    public companion object {
        public val EMPTY: SidebarComponent = withNoScore(Component.empty())

        public fun withNoScore(component: Component): SidebarComponent {
            return SidebarComponent(component, BlankFormat.INSTANCE)
        }

        public fun withCustomScore(component: Component, score: Component): SidebarComponent {
            return SidebarComponent(component, FixedFormat(score))
        }
    }
}
