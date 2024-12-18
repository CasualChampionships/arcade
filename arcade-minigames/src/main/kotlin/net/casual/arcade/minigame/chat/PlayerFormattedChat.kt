/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.chat

import net.minecraft.network.chat.Component

public data class PlayerFormattedChat(
    public val message: Component,
    public val prefix: Component? = null
)