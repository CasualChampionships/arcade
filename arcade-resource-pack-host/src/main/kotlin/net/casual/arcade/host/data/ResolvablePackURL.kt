/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.data

import net.casual.arcade.host.GlobalPackHost
import net.casual.arcade.host.ducks.ConnectionAddressHolder
import net.casual.arcade.utils.ServerUtils
import net.minecraft.server.network.ServerCommonPacketListenerImpl

/**
 * This represents a pack url that may not be resolved yet.
 *
 * The reason we may have unresolved packs is that packs
 * could be hosted locally, and we don't know what the server's
 * public ip is.
 *
 * When sending packs to the player, we know what host they used
 * to connect to the server, so we can use that to direct to the pack.
 */
public sealed interface ResolvablePackURL {
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
         * @return The [ResolvablePackURL] instance.
         */
        public fun from(url: String): ResolvablePackURL {
            return Raw(url)
        }

        internal fun local(protocol: String, ip: String?, port: Int?, path: String): ResolvablePackURL {
            return Local(protocol, ip, port, path)
        }
    }

    private data class Raw(val url: String): ResolvablePackURL {
        override fun resolve(): String {
            return this.url
        }
    }

    private data class Local(
        val protocol: String,
        val ip: String?,
        val port: Int?,
        val path: String
    ): ResolvablePackURL {
        override fun resolve(): String {
            return this.resolve(GlobalPackHost.getConfiguredIp() ?: this.ip ?: LOCALHOST)
        }

        override fun resolve(connection: ServerCommonPacketListenerImpl): String {
            val ip = GlobalPackHost.getConfiguredIp()
                ?: (connection as ConnectionAddressHolder).`arcade$getConnectionAddress`()?.key()
            return this.resolve(ip ?: LOCALHOST)
        }

        private fun resolve(ip: String): String {
            val port = this.port ?: ServerUtils.getServerOrNull()?.port ?: 25565
            return "${this.protocol}://${ip}:${port}/${this.path}"
        }

        companion object {
            const val LOCALHOST = "127.0.0.1"
        }
    }
}