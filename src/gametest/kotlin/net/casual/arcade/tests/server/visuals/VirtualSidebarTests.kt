/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.visuals

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.virtual.visuals.ArcadeVirtualVisuals
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponents
import net.casual.arcade.virtual.visuals.sidebar.VirtualSidebar
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundResetScorePacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket

@Suppress("FunctionName", "Unused")
object VirtualSidebarTests: ArcadeTestSuite() {
    override val namespace: String = ArcadeVirtualVisuals.MOD_ID

    @GameTest(maxTicks = 400)
    fun `sidebar height matches highest row`(context: TestContext) = context.test {
        val player = player().spawn()

        val sidebar = VirtualSidebar()
        sidebar.row(0).set(SidebarComponent.withNoScore(Component.literal("bottom")))
        sidebar.row(2).set(SidebarComponent.withNoScore(Component.literal("top")))

        sidebar.startObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)

        // Rows 0, 1 and 2 are displayed, even though row 1 was never set
        player.sent<ClientboundSetScorePacket>().size shouldEqual 3
    }

    @GameTest(maxTicks = 400)
    fun `unset sidebar row is blank`(context: TestContext) = context.test {
        val player = player().spawn()

        val sidebar = VirtualSidebar()
        sidebar.row(1).set(SidebarComponent.withNoScore(Component.literal("top")))

        sidebar.startObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)

        val scores = player.sent<ClientboundSetScorePacket>().associateBy { it.score() }
        val blank = assertNotNull(scores[0], "Expected row 0 to be sent")
        blank.display().orElse(null) shouldEqual Component.empty()
    }

    @GameTest(maxTicks = 400)
    fun `unset sidebar has no rows`(context: TestContext) = context.test {
        val player = player().spawn()

        val sidebar = VirtualSidebar()
        sidebar.startObservingAndSendPackets(player.asObserver())
        delay(1.Ticks)

        player.assertNotSent<ClientboundSetScorePacket>()
    }

    @GameTest(maxTicks = 400)
    fun `only changed sidebar rows are sent`(context: TestContext) = context.test {
        val player = player().spawn()

        val sidebar = VirtualSidebar()
        sidebar.setRows(SidebarComponents.of(
            SidebarComponent.withNoScore(Component.literal("first")),
            SidebarComponent.withNoScore(Component.literal("second")),
            SidebarComponent.withNoScore(Component.literal("third"))
        ))
        sidebar.startObservingAndSendPackets(player.asObserver())
        sidebar.tick()
        delay(1.Ticks)
        player.clearPackets()

        sidebar.row(0).set(SidebarComponent.withNoScore(Component.literal("changed")))
        sidebar.tick()
        delay(1.Ticks)

        val scores = player.sent<ClientboundSetScorePacket>()
        scores.size shouldEqual 1
        scores[0].score() shouldEqual 0
    }

    @GameTest(maxTicks = 400)
    fun `clearing top sidebar row resets scores`(context: TestContext) = context.test {
        val player = player().spawn()

        val sidebar = VirtualSidebar()
        sidebar.row(0).set(SidebarComponent.withNoScore(Component.literal("bottom")))
        sidebar.row(1).set(SidebarComponent.withNoScore(Component.literal("top")))
        sidebar.startObservingAndSendPackets(player.asObserver())
        sidebar.tick()
        delay(1.Ticks)
        player.clearPackets()

        sidebar.row(1).set(SidebarComponent.NONE)
        sidebar.tick()
        delay(1.Ticks)

        // Row 1 no longer exists, so it must be reset, and row 0 is untouched
        val reset = player.sent<ClientboundResetScorePacket>()
        assertTrue(reset.isNotEmpty(), "Expected the removed rows to be reset")
        player.assertNotSent<ClientboundSetScorePacket>()
    }

    @GameTest(maxTicks = 400)
    fun `setting sidebar rows clears extra rows`(context: TestContext) = context.test {
        val player = player().spawn()

        val sidebar = VirtualSidebar()
        sidebar.setRows(SidebarComponents.of(
            SidebarComponent.withNoScore(Component.literal("first")),
            SidebarComponent.withNoScore(Component.literal("second")),
            SidebarComponent.withNoScore(Component.literal("third"))
        ))
        sidebar.startObservingAndSendPackets(player.asObserver())
        sidebar.tick()
        delay(1.Ticks)
        player.clearPackets()

        sidebar.setRows(listOf(SidebarComponent.withNoScore(Component.literal("only"))))
        sidebar.tick()
        delay(1.Ticks)

        assertTrue(player.sent<ClientboundResetScorePacket>().isNotEmpty(), "Expected the removed rows to be reset")
        player.sent<ClientboundSetScorePacket>().size shouldEqual 1
    }

    @GameTest(maxTicks = 400)
    fun `sidebar row override is only sent to that player`(context: TestContext) = context.test {
        val overridden = player().spawn()
        val other = player().spawn()

        val sidebar = VirtualSidebar()
        sidebar.row(0).set(SidebarComponent.withNoScore(Component.literal("shared")))
        sidebar.startObservingAndSendPackets(overridden.asObserver())
        sidebar.startObservingAndSendPackets(other.asObserver())
        sidebar.tick()
        delay(1.Ticks)
        overridden.clearPackets()
        other.clearPackets()

        sidebar.row(0).set(overridden, SidebarComponent.withNoScore(Component.literal("yours")))
        sidebar.tick()
        delay(1.Ticks)

        val scores = overridden.sent<ClientboundSetScorePacket>()
        scores.size shouldEqual 1
        scores[0].display().orElse(null) shouldEqual Component.literal("yours")

        other.assertNotSent<ClientboundSetScorePacket>()
    }
}
