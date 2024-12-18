/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import net.minecraft.server.MinecraftServer
import java.util.*

public class MinigameCreationContext(
    public val server: MinecraftServer,
    public val uuid: UUID = UUID.randomUUID()
)