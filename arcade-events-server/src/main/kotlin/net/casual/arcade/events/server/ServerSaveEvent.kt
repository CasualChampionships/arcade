/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server

import net.casual.arcade.events.common.Event
import net.minecraft.server.MinecraftServer

public data class ServerSaveEvent(
    val server: MinecraftServer,
    val stopping: Boolean
): Event