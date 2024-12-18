/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server

import net.casual.arcade.events.common.Event
import net.minecraft.server.MinecraftServer

/**
 * This event is fired just before the [MinecraftServer]
 * started its first tick. Everything by this point should
 * be loaded, for example, worlds.
 *
 * @param server The [MinecraftServer] instance that is loaded.
 */
public data class ServerLoadedEvent(
    /**
     * The [MinecraftServer] instance that is loaded.
     */
    val server: MinecraftServer
): Event