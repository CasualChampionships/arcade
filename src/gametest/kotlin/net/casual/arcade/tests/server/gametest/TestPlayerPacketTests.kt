/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket

object TestPlayerPacketTests: ArcadeTestSuite() {
    @GameTest(maxTicks = 400)
    fun fakePlayerCapturesClientboundPackets(context: ArcadeTestContext) = context.test {
        val player = createTestPlayer()

        val message = Component.literal("You've been poked!")
        val packet = ClientboundSystemChatPacket(message, false)
        player.assertNotSent(packet)

        player.sendSystemMessage(message)
        delay(1.Ticks)

        player.assertSent(packet)
    }
}
