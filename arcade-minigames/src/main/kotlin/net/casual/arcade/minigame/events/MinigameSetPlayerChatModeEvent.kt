/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.chat.MinigameChatMode
import net.minecraft.server.level.ServerPlayer

public data class MinigameSetPlayerChatModeEvent(
    override val minigame: Minigame,
    val player: ServerPlayer,
    val mode: MinigameChatMode,
    val feedback: Boolean
): CancellableEvent.Default(), MinigameEvent