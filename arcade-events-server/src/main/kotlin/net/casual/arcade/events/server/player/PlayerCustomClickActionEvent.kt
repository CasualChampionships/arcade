/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

public data class PlayerCustomClickActionEvent(
    override val player: ServerPlayer,
    val id: ResourceLocation,
    val payload: Tag?
): PlayerEvent {
    private var consumed: Boolean = false

    public fun consume(): Boolean {
        if (this.consumed) {
            return false
        }
        this.consumed = true
        return true
    }

    public fun consumed(): Boolean {
        return this.consumed
    }
}