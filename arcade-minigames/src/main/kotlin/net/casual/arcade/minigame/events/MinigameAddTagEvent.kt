/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

public data class MinigameAddTagEvent(
    override val minigame: Minigame,
    val player: ServerPlayer,
    val tag: ResourceLocation
): MinigameEvent