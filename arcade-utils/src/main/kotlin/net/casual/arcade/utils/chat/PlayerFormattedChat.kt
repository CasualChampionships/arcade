/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.chat

import net.casual.arcade.utils.ComponentUtils.isEmpty
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

public data class PlayerFormattedChat(
    public val prefix: Component = CommonComponents.EMPTY,
    public val username: Component? = null,
    public val message: Component,
) {
    public fun asComponent(fallbackUsername: () -> Component): MutableComponent {
        val decorated = Component.empty()
        if (!this.prefix.isEmpty()) {
            decorated.append(this.prefix).append(CommonComponents.SPACE)
        }
        val username = this.username ?: fallbackUsername.invoke()
        if (!username.isEmpty()) {
            decorated.append(username).append(CommonComponents.SPACE)
        }
        return decorated.append(this.message)
    }
}