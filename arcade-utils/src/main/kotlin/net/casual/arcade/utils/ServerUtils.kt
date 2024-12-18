/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.util.ducks.CustomMOTD
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import org.jetbrains.annotations.ApiStatus.Internal

public object ServerUtils {
    private var instance: MinecraftServer? = null

    /**
     * Gets the [MinecraftServer] instance, this should only
     * be called after the server has been created otherwise
     * this method will throw an [IllegalStateException].
     *
     * This should typically be avoided if you already have a
     * reference to the [MinecraftServer].
     *
     * @return The [MinecraftServer] instance.
     * @see getServerOrNull
     */
    @JvmStatic
    public fun getServer(): MinecraftServer {
        return this.getServerOrNull()
            ?: throw IllegalStateException("Called ServerUtils.getServer before server was created")
    }

    /**
     * Gets the [MinecraftServer] instance, this may be `null`
     * if the server has not been created yet.
     *
     * This should typically be avoided if you already have a
     * reference to the [MinecraftServer].
     *
     * @return The [MinecraftServer] instance, or null.
     * @see getServer
     */
    @JvmStatic
    public fun getServerOrNull(): MinecraftServer? {
        return this.instance
    }

    @JvmStatic
    public fun MinecraftServer.nether(): ServerLevel {
        return this.getLevel(Level.NETHER)!!
    }

    @JvmStatic
    public fun MinecraftServer.end(): ServerLevel {
        return this.getLevel(Level.END)!!
    }

    public fun MinecraftServer.setMessageOfTheDay(message: Component) {
        (this as CustomMOTD).`arcade$setMOTD`(message)
    }

    public fun MinecraftServer.getMessageOfTheDay(): Component {
        val custom = (this as CustomMOTD).`arcade$getMOTD`()
        return custom ?: Component.nullToEmpty(this.motd)
    }

    @Internal
    @JvmStatic
    public fun setServer(server: MinecraftServer) {
        this.instance = server
    }
}