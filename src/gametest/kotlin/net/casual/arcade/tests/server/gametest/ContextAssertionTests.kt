/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.tests.server.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.player.username
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestAssertException

/**
 * Tests for the assertion helpers themselves.
 */
@Suppress("FunctionName", "Unused")
object ContextAssertionTests: ArcadeTestSuite() {
    override val namespace: String = "arcade-gametest"

    @GameTest
    fun `true assertions pass`(context: TestContext) = context.test {
        assertTrue(true)
        assertFalse(false)
        assertEquals(1, 1)
        assertNotEquals(1, 2)
        assertNull(null)
        assertEquals("value", assertNotNull("value"))
    }

    @GameTest
    fun `false assertions throw`(context: TestContext) = context.test {
        assertThrows<GameTestAssertException> { assertTrue(false) }
        assertThrows<GameTestAssertException> { assertFalse(true) }
        assertThrows<GameTestAssertException> { assertEquals(1, 2) }
        assertThrows<GameTestAssertException> { assertNotEquals(1, 1) }
        assertThrows<GameTestAssertException> { assertNull("value") }
        assertThrows<GameTestAssertException> { assertNotNull(null) }
    }

    @GameTest
    fun `true infix assertions pass`(context: TestContext) = context.test {
        1 shouldEqual 1
        "value" shouldEqual "value"
        1 shouldNotEqual 2
        null shouldEqual null
    }

    @GameTest
    fun `false infix assertions throw`(context: TestContext) = context.test {
        assertThrows<GameTestAssertException> { 1 shouldEqual 2 }
        assertThrows<GameTestAssertException> { 1 shouldNotEqual 1 }
    }

    @GameTest(maxTicks = 200)
    fun `true eventually assertions passes`(context: TestContext) = context.test {
        val target = server.tickCount + 3
        assertEventually(2.Seconds) { server.tickCount >= target }
    }

    @GameTest(maxTicks = 200)
    fun `false eventually assertions throw`(context: TestContext) = context.test {
        assertThrows<GameTestAssertException> {
            assertEventually(5.Ticks) { false }
        }
    }

    @GameTest(maxTicks = 400)
    fun `test players are unique`(context: TestContext) = context.test {
        val first = player().spawn()
        val second = player().spawn()

        assertNotEquals(first.username, second.username)
        assertNotEquals(first.uuid, second.uuid)
    }
}
