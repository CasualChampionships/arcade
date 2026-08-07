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
import net.casual.arcade.utils.player.kick
import net.casual.arcade.virtual.visuals.bossbar.VirtualBossbar
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.arcade.virtual.visuals.sidebar.VirtualSidebar
import net.casual.arcade.virtual.visuals.utils.observingVisuals
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket

object VirtualVisualObservingTests: ArcadeTestSuite() {
    @GameTest(maxTicks = 400)
    fun observingTracksEveryVisualAnObserverIsShown(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()
        val observer = player.asObserver()

        val bossbar = VirtualBossbar()
        val sidebar = VirtualSidebar()

        observer.observingVisuals().size shouldEqual 0

        bossbar.startObservingAndSendPackets(observer)
        sidebar.startObservingAndSendPackets(observer)
        observer.observingVisuals().size shouldEqual 2

        bossbar.stopObservingAndSendPackets(observer)
        observer.observingVisuals().size shouldEqual 1
    }

    @GameTest(maxTicks = 400)
    fun observingASecondSidebarReplacesTheFirst(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()
        val observer = player.asObserver()

        val first = VirtualSidebar()
        first.row(0).set(SidebarComponent.withNoScore(Component.literal("first")))
        val second = VirtualSidebar()
        second.row(0).set(SidebarComponent.withNoScore(Component.literal("second")))

        first.startObservingAndSendPackets(observer)
        second.startObservingAndSendPackets(observer)
        delay(1.Ticks)

        assertFalse(first.observers.isObserving(observer), "Expected the first sidebar to have been replaced")
        assertTrue(second.observers.isObserving(observer), "Expected the second sidebar to be observed")
        observer.observingVisuals().size shouldEqual 1

        // The replacement's rows are the last thing sent
        val scores = player.sent<ClientboundSetScorePacket>()
        val last = assertNotNull(scores.lastOrNull(), "Expected the second sidebar's rows to be sent")
        last.display().orElse(null) shouldEqual Component.literal("second")
    }

    @GameTest(maxTicks = 400)
    fun leavingStopsObservingEveryVisual(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()
        val observer = player.asObserver()

        val bossbar = VirtualBossbar()
        val sidebar = VirtualSidebar()
        bossbar.startObservingAndSendPackets(observer)
        sidebar.startObservingAndSendPackets(observer)

        player.kick()
        delay(1.Ticks)

        assertFalse(bossbar.observers.isObserving(observer), "Expected the bossbar to drop the observer")
        assertFalse(sidebar.observers.isObserving(observer), "Expected the sidebar to drop the observer")
        observer.observingVisuals().size shouldEqual 0
    }

    @GameTest(maxTicks = 400)
    fun visualsNoLongerTicksPacketsToAnObserverWhoLeft(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()
        val observer = player.asObserver()

        val bossbar = VirtualBossbar()
        bossbar.startObservingAndSendPackets(observer)

        player.kick()
        delay(1.Ticks)
        player.clearPackets()

        bossbar.title.set(Component.literal("Updated!"))
        bossbar.tick()
        delay(1.Ticks)

        player.assertNotSent<ClientboundBossEventPacket>()
    }

    @GameTest(maxTicks = 400)
    fun observingTheSameVisualTwiceIsANoop(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()
        val observer = player.asObserver()

        val bossbar = VirtualBossbar()
        bossbar.startObservingAndSendPackets(observer)
        player.clearPackets()

        bossbar.startObservingAndSendPackets(observer)
        delay(1.Ticks)

        observer.observingVisuals().size shouldEqual 1
        player.assertNotSent<ClientboundBossEventPacket>()
    }
}
