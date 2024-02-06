package net.casual.arcade.chat

import net.minecraft.network.chat.Component

public data class FormattedChat(
    public val message: Component,
    public val prefix: Component? = null
)