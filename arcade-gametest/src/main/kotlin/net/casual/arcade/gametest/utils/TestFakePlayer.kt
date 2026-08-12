/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.utils

import com.mojang.authlib.GameProfile
import io.netty.channel.embedded.EmbeddedChannel
import net.casual.arcade.gametest.TestContext
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.network.FakeConnection
import net.casual.arcade.npc.network.FakeGamePacketListenerImpl
import net.casual.arcade.utils.getDebugName
import net.casual.arcade.utils.player.username
import net.minecraft.network.Connection
import net.minecraft.gametest.framework.GameTestAssertException
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.network.CommonListenerCookie

/**
 * A [net.casual.arcade.npc.FakePlayer] that records the clientbound packets sent to it.
 *
 * This is useful for testing packet based features, especially when packets
 * are modified via events/mixins. The packets are recorded via the [net.minecraft.network.Connection]'s
 * channel, so any mixins into [net.minecraft.server.network.ServerCommonPacketListenerImpl.send]
 * are accounted for.
 */
public class TestFakePlayer(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile,
    info: ClientInformation
): FakePlayer(server, level, profile, info) {
    private val recorded = ArrayList<Packet<*>>()

    public var context: TestContext? = null

    private lateinit var channel: EmbeddedChannel

    override fun createConnection(
        server: MinecraftServer,
        connection: Connection,
        cookie: CommonListenerCookie
    ): FakeGamePacketListenerImpl {
        this.channel = (connection as FakeConnection).embedded
        return object: FakeGamePacketListenerImpl(server, connection, this, cookie) {
            override fun receivesPackets(): Boolean {
                return true
            }
        }
    }

    public fun fail(message: Component): Nothing {
        val context = this.context
        context?.fail(message)
        throw GameTestAssertException(message, 0)
    }

    public fun packets(): List<Packet<*>> {
        this.drain()
        return this.recorded
    }

    /**
     * Discards recorded packets.
     */
    public fun clearPackets() {
        this.drain()
        this.recorded.clear()
    }

    public fun packetsAsString(): String {
        return this.packets().joinToString { packet -> packet.getDebugName() }
    }

    public inline fun <reified T: Packet<*>> sent(predicate: (T) -> Boolean = { true }): List<T> {
        return this.packets().filterIsInstance<T>().filter(predicate)
    }

    public inline fun <reified T: Packet<*>> assertSent(predicate: (T) -> Boolean = { true }): T {
        return this.sent(predicate).firstOrNull() ?: this.fail(Component.literal(
            "Expected ${T::class.java.simpleName} sent to ${this.username}, saw: ${this.packetsAsString()}"
        ))
    }

    public fun assertSent(packet: Packet<*>) {
        if (!this.packets().contains(packet)) {
            this.fail(Component.literal(
                "Expected ${packet.getDebugName()} sent to ${this.username}, saw: ${this.packetsAsString()}"
            ))
        }
    }

    public inline fun <reified T: Packet<*>> assertNotSent(predicate: (T) -> Boolean = { true }) {
        val found = this.sent(predicate)
        if (found.isNotEmpty()) {
            this.fail(Component.literal(
                "Expected no ${T::class.java.simpleName} sent to ${this.username}, saw ${this.packetsAsString()}"
            ))
        }
    }

    public fun assertNotSent(packet: Packet<*>) {
        if (this.packets().contains(packet)) {
            this.fail(Component.literal(
                "Expected no ${packet.getDebugName()} sent to ${this.username}, saw: ${this.packetsAsString()}"
            ))
        }
    }

    private fun drain() {
        this.channel.flush()
        while (true) {
            val message = this.channel.readOutbound<Any>() ?: break
            if (message is Packet<*>) {
                this.recorded.add(message)
            }
        }
    }
}