/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.scheduler.ArcadeScheduler
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket

@Suppress("FunctionName", "Unused")
object TestPlayerPacketTests: ArcadeTestSuite() {
    override val namespace: String = "arcade-gametest"

    @GameTest(maxTicks = 200)
    fun `test player records packets`(context: TestContext) = context.test {
        val player = createTestPlayer()

        val message = Component.literal("You've been poked!")
        val packet = ClientboundSystemChatPacket(message, false)
        player.assertNotSent(packet)

        player.sendSystemMessage(message)
        delay(1.Ticks)

        player.assertSent(packet)
    }
}
