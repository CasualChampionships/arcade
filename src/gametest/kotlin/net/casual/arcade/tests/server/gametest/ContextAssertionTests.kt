/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.player.username
import net.fabricmc.fabric.api.gametest.v1.GameTest
import net.minecraft.gametest.framework.GameTestAssertException

/**
 * Tests for the assertion helpers themselves.
 */
object ContextAssertionTests: ArcadeTestSuite() {
    @GameTest
    fun assertionsPassWhenSatisfied(context: ArcadeTestContext) = context.test {
        assertTrue(true)
        assertFalse(false)
        assertEquals(1, 1)
        assertNotEquals(1, 2)
        assertNull(null)
        assertEquals("value", assertNotNull("value"))
    }

    @GameTest
    fun assertionsThrowWhenViolated(context: ArcadeTestContext) = context.test {
        assertThrows<GameTestAssertException> { assertTrue(false) }
        assertThrows<GameTestAssertException> { assertFalse(true) }
        assertThrows<GameTestAssertException> { assertEquals(1, 2) }
        assertThrows<GameTestAssertException> { assertNotEquals(1, 1) }
        assertThrows<GameTestAssertException> { assertNull("value") }
        assertThrows<GameTestAssertException> { assertNotNull(null) }
    }

    @GameTest
    fun infixAssertionsPassWhenSatisfied(context: ArcadeTestContext) = context.test {
        1 shouldEqual 1
        "value" shouldEqual "value"
        1 shouldNotEqual 2
        null shouldEqual null
    }

    @GameTest
    fun infixAssertionsThrowWhenViolated(context: ArcadeTestContext) = context.test {
        assertThrows<GameTestAssertException> { 1 shouldEqual 2 }
        assertThrows<GameTestAssertException> { 1 shouldNotEqual 1 }
    }

    @GameTest(maxTicks = 200)
    fun assertEventuallyPassesOnceConditionHolds(context: ArcadeTestContext) = context.test {
        val target = server.tickCount + 3
        assertEventually(2.Seconds) { server.tickCount >= target }
    }

    @GameTest(maxTicks = 200)
    fun assertEventuallyFailsAfterTimeout(context: ArcadeTestContext) = context.test {
        assertThrows<GameTestAssertException> {
            assertEventually(5.Ticks) { false }
        }
    }

    @GameTest(maxTicks = 400)
    fun generatedPlayerNamesAreUnique(context: ArcadeTestContext) = context.test {
        val first = createTestPlayer()
        val second = createTestPlayer()

        assertNotEquals(first.username, second.username)
        assertNotEquals(first.uuid, second.uuid)
    }
}
