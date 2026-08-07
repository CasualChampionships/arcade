/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.visuals

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.player.username
import net.casual.arcade.virtual.visuals.tab.DynamicVirtualPlayerList
import net.casual.arcade.virtual.visuals.tab.PlayerListEntries
import net.casual.arcade.virtual.visuals.tab.VirtualPlayerList
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundTabListPacket

object VirtualPlayerListTests: ArcadeTestSuite() {
    @GameTest(maxTicks = 400)
    fun observerIsSentTheHeaderAndFooter(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()

        val list = VirtualPlayerList(server, FixedEntries)
        list.header.set(Component.literal("Header"))
        list.footer.set(Component.literal("Footer"))
        list.startObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)

        val packet = player.assertSent<ClientboundTabListPacket>()
        packet.header() shouldEqual Component.literal("Header")
        packet.footer() shouldEqual Component.literal("Footer")
    }

    @GameTest(maxTicks = 400)
    fun aHeaderOverrideIsOnlySentToThatPlayer(context: ArcadeTestContext) = context.test {
        val overridden = createTestPlayer()
        val other = createTestPlayer()

        val list = VirtualPlayerList(server, FixedEntries)
        list.header.set(Component.literal("Shared"))
        list.startObservingAndSendPackets(overridden.asObserver())
        list.startObservingAndSendPackets(other.asObserver())
        list.tick()
        delay(1.Ticks)
        overridden.clearPackets()
        other.clearPackets()

        list.header.set(overridden, Component.literal("Yours"))
        list.tick()
        delay(1.Ticks)

        val packet = overridden.assertSent<ClientboundTabListPacket>()
        packet.header() shouldEqual Component.literal("Yours")

        other.assertNotSent<ClientboundTabListPacket>()
    }

    @GameTest(maxTicks = 400)
    fun anUnchangedHeaderSendsNothing(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()

        val list = VirtualPlayerList(server, FixedEntries)
        list.header.set(Component.literal("Header"))
        list.startObservingAndSendPackets(player.asObserver())
        list.tick()
        delay(1.Ticks)
        player.clearPackets()

        list.header.set(Component.literal("Header"))
        list.tick()
        delay(1.Ticks)

        player.assertNotSent<ClientboundTabListPacket>()
    }

    @GameTest(maxTicks = 400)
    fun elementsGenerateTheHeaderAndFooter(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()

        val list = DynamicVirtualPlayerList(server, FixedEntries)
        list.setHeader(UniversalElement { Component.literal("Shared") })
        list.setFooter(PlayerSpecificElement { observee -> Component.literal(observee.username) })
        list.startObservingAndSendPackets(player.asObserver())
        list.tick()
        delay(1.Ticks)

        val packet = assertNotNull(
            player.sent<ClientboundTabListPacket>().lastOrNull(),
            "Expected a tab list packet"
        )
        packet.header() shouldEqual Component.literal("Shared")
        packet.footer() shouldEqual Component.literal(player.username)
    }

    @GameTest(maxTicks = 400)
    fun generatedOverridesAreDiscardedWhenAnObserverStopsObserving(
        context: ArcadeTestContext
    ) = context.test {
        val player = createTestPlayer()

        val list = DynamicVirtualPlayerList(server, FixedEntries)
        list.setHeader(PlayerSpecificElement { Component.literal("Generated") })
        list.startObservingAndSendPackets(player.asObserver())
        list.tick()
        delay(1.Ticks)

        assertTrue(list.header.isOverridden(player), "Expected the element to have generated an override")

        list.stopObservingAndSendPackets(player.asObserver())

        assertFalse(list.header.isOverridden(player), "Expected the generated override to be discarded")
    }

    private object FixedEntries: PlayerListEntries {
        override val size: Int = 2

        override fun getEntryAt(index: Int): PlayerListEntries.Entry {
            return PlayerListEntries.Entry(Component.literal("Entry $index"))
        }
    }
}
