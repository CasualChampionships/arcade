/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.hidden

import net.casual.arcade.utils.PlayerUtils.levelServer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public class HiddenCommandContext(
    public val player: ServerPlayer
) {
    private var removed = false

    public val server: MinecraftServer
        get() = this.player.levelServer

    public fun remove() {
        this.removed = true
    }

    internal fun removed(): Boolean {
        return this.removed
    }
}
