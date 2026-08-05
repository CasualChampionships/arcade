/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.gametest

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.phase.BuiltInEventPhases.PRE
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.gametest.ArcadeTestContext
import net.casual.arcade.gametest.ArcadeTestSuite
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.coroutine.launch
import net.fabricmc.fabric.api.gametest.v1.GameTest

object CoroutineTests: ArcadeTestSuite() {
    @GameTest(maxTicks = 100)
    fun delayResumes(context: ArcadeTestContext) = context.test {
        delay(1.Ticks)
    }

    @GameTest(maxTicks = 100)
    fun delayResumesLater(context: ArcadeTestContext) = context.test {
        val before = server.tickCount
        delay(5.Ticks)
        assertTrue(server.tickCount > before, "Server tickCount was same before and after delay!")
    }

    @GameTest(maxTicks = 200)
    fun delayConsistencyAfterResuming(context: ArcadeTestContext) = context.test {
        val first = CompletableDeferred<Int>()
        val second = CompletableDeferred<Int>()

        val start = server.tickCount
        server.launch {
            // Issued from outside the coroutine tick
            delay(5.Ticks)
            first.complete(server.tickCount)
            // Issued from *inside* the coroutine tick
            delay(5.Ticks)
            second.complete(server.tickCount)
        }

        val a = first.await()
        val b = second.await()
        assertEquals(5, a - start, "A delay issued from outside a tick did not take its full duration")
        assertEquals(5, b - a, "A delay issued from inside a tick did not take its full duration")
    }

    @GameTest(maxTicks = 200)
    fun delayConsistencyFromTickPre(context: ArcadeTestContext) = context.test {
        val started = CompletableDeferred<Int>()
        val finished = CompletableDeferred<Int>()

        GlobalEventHandler.Server.register<ServerTickEvent>(phase = PRE) {
            if (!started.isCompleted) {
                started.complete(server.tickCount)
                server.launch {
                    delay(5.Ticks)
                    finished.complete(server.tickCount)
                }
            }
        }

        val from = started.await()
        val to = finished.await()
        assertEquals(5, to - from, "A delay issued early in the tick took ${to - from} ticks")
    }

    @GameTest(maxTicks = 200)
    fun delayOffThreadThrows(context: ArcadeTestContext) = context.test {
        val result = CompletableDeferred<Throwable?>()

        server.launch {
            try {
                withContext(Dispatchers.Default) {
                    delay(1.Ticks)
                }
                result.complete(null)
            } catch (e: Throwable) {
                result.complete(e)
            }
        }

        val thrown = assertNotNull(result.await(), "delay resumed a coroutine which had left the Minecraft thread")
        assertTrue(
            thrown is IllegalStateException,
            "delay threw ${thrown::class.simpleName} rather than IllegalStateException"
        )
    }
}
