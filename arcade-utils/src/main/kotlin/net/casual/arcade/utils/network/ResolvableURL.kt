/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.network

import net.casual.arcade.util.ducks.ConnectionAddressHolder
import net.casual.arcade.util.mixins.network.ServerCommonPacketListenerAccessor
import net.casual.arcade.utils.ServerUtils
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerCommonPacketListenerImpl

/**
 * This represents an url that may not be resolved yet.
 *
 * The reason we may have unresolved urls is that things
 * could be hosted locally, and we don't know what the server's
 * public ip is.
 *
 * When sending packs to the player, we know what host they used
 * to connect to the server, so we can use that to direct to the pack.
 */
public sealed interface ResolvableURL {
    /**
     * Resolves the url with no context.
     *
     * @return The resolved pack url.
     */
    public fun resolve(): String

    /**
     * Resolves the url with the player's connection context.
     *
     * @return The resolved pack url.
     */
    public fun resolve(connection: ServerCommonPacketListenerImpl): String {
        return this.resolve()
    }

    public companion object {
        /**
         * Creates a resolved pack url.
         *
         * @param url The pack url.
         * @return The [ResolvableURL] instance.
         */
        public fun from(url: String): ResolvableURL {
            return Raw(url)
        }

        public fun local(protocol: String, ip: String?, port: Int?, path: String): ResolvableURL {
            return Local(protocol, ip, port, path)
        }
    }

    private data class Raw(val url: String): ResolvableURL {
        override fun resolve(): String {
            return this.url
        }
    }

    private data class Local(
        val protocol: String,
        val ip: String?,
        val port: Int?,
        val path: String
    ): ResolvableURL {
        override fun resolve(): String {
            return this.resolve(HostIP.get() ?: this.ip ?: HostIP.LOCALHOST)
        }

        override fun resolve(connection: ServerCommonPacketListenerImpl): String {
            val ip = HostIP.get() ?: (connection as ConnectionAddressHolder).`arcade$getConnectionAddress`()?.key()
            val server = (connection as ServerCommonPacketListenerAccessor).server
            return this.resolve(ip ?: HostIP.LOCALHOST, server)
        }

        private fun resolve(ip: String, server: MinecraftServer? = ServerUtils.getServerOrNull()): String {
            val port = this.port ?: server?.port ?: 25565
            return "${this.protocol}://${ip}:${port}/${this.path}"
        }
    }
}