/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.server

import net.casual.arcade.utils.server.ServerSingleton.get
import net.casual.arcade.utils.server.ServerSingleton.getOrNull
import net.minecraft.core.RegistryAccess
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus.Internal

public object ServerSingleton {
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
     * @see getOrNull
     */
    @JvmStatic
    public fun get(): MinecraftServer {
        return this.getOrNull()
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
     * @see get
     */
    @JvmStatic
    public fun getOrNull(): MinecraftServer? {
        return this.instance
    }

    @JvmStatic
    public fun isOnServerThread(): Boolean {
        return this.getOrNull()?.isSameThread ?: false
    }

    @JvmStatic
    public fun getRegistryAccessOrEmpty(): RegistryAccess {
        return this.getOrNull()?.registryAccess() ?: RegistryAccess.EMPTY
    }

    @Internal
    @JvmStatic
    public fun setServer(server: MinecraftServer?) {
        this.instance = server
    }
}