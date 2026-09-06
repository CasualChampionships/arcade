/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.arcade
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import java.util.*

class TransientTestMinigame(
    server: MinecraftServer,
    uuid: UUID
): Minigame(server, uuid, ID, TestMinigamePhase.entries) {
    companion object {
        val ID: Identifier = arcade("transient_test_minigame")
    }
}
