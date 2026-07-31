/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer

import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.network.PacketSender
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel

public interface Observer: PacketSender {
    public val context: Context
    public val location: LocationWithLevel<ServerLevel>

    public override fun send(packet: Packet<*>)

    public class Context {
        private val map = HashMap<Key<*>, Any>()

        public fun <T: Any> set(key: Key<T>, value: T) {
            this.map[key] = value
        }

        public fun <T: Any> get(key: Key<T>): T? {
            @Suppress("unchecked_cast")
            return this.map[key] as T?
        }

        public fun <T: Any> getOrSet(key: Key<T>, value: () -> T): T {
            var existing = this.get(key)
            if (existing == null) {
                existing = value.invoke()
                this.set(key, existing)
            }
            return existing
        }

        public data class Key<@Suppress("unused") T>(val id: Identifier)
    }
}