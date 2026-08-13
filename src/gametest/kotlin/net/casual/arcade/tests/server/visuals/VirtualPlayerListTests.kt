/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.visuals

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.utils.ClientboundPlayerInfoUpdatePacket
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.player.username
import net.casual.arcade.virtual.visuals.ArcadeVirtualVisuals
import net.casual.arcade.virtual.visuals.tab.DynamicVirtualPlayerList
import net.casual.arcade.virtual.visuals.tab.PlayerListEntries
import net.casual.arcade.virtual.visuals.tab.VirtualPlayerList
import net.casual.arcade.virtual.visuals.elements.PlayerSpecificElement
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import java.util.*

@Suppress("FunctionName", "Unused")
object VirtualPlayerListTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeVirtualVisuals.MOD_ID

    @GameTest(maxTicks = 400)
    fun `header and footer are sent when observing`(context: TestContext) = context.test {
        val player = player().spawn()

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
    fun `header override is only sent to that player`(context: TestContext) = context.test {
        val overridden = player().spawn()
        val other = player().spawn()

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
    fun `unchanged header sends nothing`(context: TestContext) = context.test {
        val player = player().spawn()

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
    fun `elements generate header and footer`(context: TestContext) = context.test {
        val player = player().spawn()

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
    fun `generated overrides are discarded when not observing`(
        context: TestContext
    ) = context.test {
        val player = player().spawn()

        val list = DynamicVirtualPlayerList(server, FixedEntries)
        list.setHeader(PlayerSpecificElement { Component.literal("Generated") })
        list.startObservingAndSendPackets(player.asObserver())
        list.tick()
        delay(1.Ticks)

        assertTrue(list.header.isOverridden(player), "Expected the element to have generated an override")

        list.stopObservingAndSendPackets(player.asObserver())

        assertFalse(list.header.isOverridden(player), "Expected the generated override to be discarded")
    }

    @GameTest(maxTicks = 400)
    fun `real players are unlisted for observers`(context: TestContext) = context.test {
        val player = player().spawn()

        val list = VirtualPlayerList(server, FixedEntries)
        list.startObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)
        player.clearPackets()

        player.connection.send(createListingPacket(player))
        delay(1.Ticks)

        val packet = player.assertSent<ClientboundPlayerInfoUpdatePacket>()
        val entry = assertNotNull(
            packet.entries().firstOrNull { it.profileId == player.uuid },
            "Expected the real player to still be in the packet"
        )
        assertFalse(entry.listed, "Expected the real player to be unlisted")
    }

    @GameTest(maxTicks = 400)
    fun `real players are listed for non observers`(context: TestContext) = context.test {
        val player = player().spawn()

        VirtualPlayerList(server, FixedEntries)
        player.clearPackets()

        player.connection.send(createListingPacket(player))
        delay(1.Ticks)

        val packet = player.assertSent<ClientboundPlayerInfoUpdatePacket>()
        val entry = assertNotNull(
            packet.entries().firstOrNull { it.profileId == player.uuid },
            "Expected the real player to be in the packet"
        )
        assertTrue(entry.listed, "Expected the real player to remain listed")
    }

    private fun createListingPacket(player: ServerPlayer): ClientboundPlayerInfoUpdatePacket {
        val entry = ClientboundPlayerInfoUpdatePacket.Entry(
            player.uuid, null, true, 0, GameType.SURVIVAL, null, true, 0, null
        )
        return ClientboundPlayerInfoUpdatePacket(EnumSet.of(Action.UPDATE_LISTED), listOf(entry))
    }

    private object FixedEntries: PlayerListEntries {
        override val size: Int = 2

        override fun getEntryAt(index: Int): PlayerListEntries.Entry {
            return PlayerListEntries.Entry(Component.literal("Entry $index"))
        }
    }
}
