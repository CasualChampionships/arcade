/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import net.minecraft.server.MinecraftServer
import java.util.*

public data class MinigameCreationContext(
    public val server: MinecraftServer,
    public val uuid: UUID = UUID.randomUUID(),
    public val reason: CreationReason
) {
    public enum class CreationReason {
        Initial,
        Reloaded
    }

    public companion object {
        public fun initial(server: MinecraftServer): MinigameCreationContext {
            return MinigameCreationContext(server, reason = CreationReason.Initial)
        }

        public fun reloaded(server: MinecraftServer, uuid: UUID): MinigameCreationContext {
            return MinigameCreationContext(server, uuid, CreationReason.Reloaded)
        }
    }
}