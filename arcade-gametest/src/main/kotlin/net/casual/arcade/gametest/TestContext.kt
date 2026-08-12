/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.core.BlockPos
import net.minecraft.core.UUIDUtil
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.UnknownGameTestException
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.GameType
import java.util.concurrent.atomic.AtomicInteger

/**
 * The receiver passed to every [TestSuite] test method.
 *
 * All state tracked here is per-test. Tests within a batch run *concurrently* in the same level.
 */
public class TestContext(public val helper: GameTestHelper) {
    private val players = ArrayList<TestFakePlayer>()

    private var failure: Throwable? = null
    private var succeeded = false

    public val server: MinecraftServer
        get() = this.helper.level.server

    public val level: ServerLevel
        get() = this.helper.level

    /**
     * Runs [block] as a coroutine on the server thread, passing the test when it returns normally
     * and failing it if it throws.
     */
    public fun test(block: suspend TestContext.() -> Unit) {
        val context = this

        val scheduler = SimpleTickedScheduler.server()
        this.helper.onEachTick {
            scheduler.tick()

            val thrown = context.failure
            if (thrown != null) {
                context.failure = null
                throw thrown as? Exception ?: UnknownGameTestException(thrown)
            }
            if (context.succeeded) {
                context.succeeded = false
                context.helper.succeed()
            }
        }

        val job = scheduler.asCoroutineScope().launch {
            try {
                context.block()
                context.succeeded = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                context.failure = e
            } finally {
                context.cleanup()
            }
        }

        this.helper.runBeforeTestEnd(job::cancel)
    }

    /**
     * Creates a [TestFakePlayer] with a generated name, unique for the lifetime of the server.
     *
     * @param recordLoginPackets Whether to record the login packets.
     * @return The created [TestFakePlayer]
     */
    public suspend fun createTestPlayer(recordLoginPackets: Boolean = false): TestFakePlayer {
        return this.createTestPlayer("TestPlayer${TEST_PLAYER_COUNTER.incrementAndGet()}", recordLoginPackets)
    }

    /**
     * Creates a [TestFakePlayer] with an offline profile who acts like a real player,
     * suspending until it has spawned.
     *
     * The [name] must be unique across concurrently running tests.
     * Use the no-argument overload to have a unique name generated.
     *
     * @param name The name of the [TestFakePlayer].
     * @param recordLoginPackets Whether to record the login packets.
     * @return The created [TestFakePlayer]
     */
    public suspend fun createTestPlayer(name: String, recordLoginPackets: Boolean = false): TestFakePlayer {
        val profile = GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name)
        val player = FakePlayer.join(this.server, profile, ::TestFakePlayer).await()
        player.context = this
        player.setGameMode(GameType.SURVIVAL)
        this.players.add(player)

        if (!recordLoginPackets) {
            player.clearPackets()
        }
        return player
    }

    /**
     * Fails the test with [message].
     *
     * @param message The message of the failure.
     */
    public fun fail(message: Component): Nothing {
        throw this.helper.assertionException(message)
    }

    public fun fail(message: String): Nothing {
        this.fail(Component.literal(message))
    }

    /**
     * Fails the test with [message], marking [pos] in the world.
     *
     * @param pos The relative position to the test structure of the error.
     * @param message The message of the failure.
     */
    public fun fail(pos: BlockPos, message: Component): Nothing {
        throw this.helper.assertionException(pos, message)
    }

    public fun fail(pos: BlockPos, message: String): Nothing {
        this.fail(pos, Component.literal(message))
    }

    public fun assertTrue(condition: Boolean, message: Component? = null) {
        if (!condition) {
            this.fail(message ?: Component.literal("Expected condition to be true"))
        }
    }

    public fun assertTrue(condition: Boolean, message: String) {
        this.assertTrue(condition, Component.literal(message))
    }

    public fun assertFalse(condition: Boolean, message: Component? = null) {
        if (condition) {
            this.fail(message ?: Component.literal("Expected condition to be false"))
        }
    }

    public fun assertFalse(condition: Boolean, message: String) {
        this.assertFalse(condition, Component.literal(message))
    }

    public fun <T> assertEquals(expected: T, actual: T, message: Component? = null) {
        if (expected != actual) {
            this.fail(message ?: Component.literal("Expected <$expected> but was <$actual>"))
        }
    }

    public fun <T> assertEquals(expected: T, actual: T, message: String) {
        this.assertEquals(expected, actual, Component.literal(message))
    }

    public fun <T> assertNotEquals(illegal: T, actual: T, message: Component? = null) {
        if (illegal == actual) {
            this.fail(message ?: Component.literal("Expected value to not be <$illegal>"))
        }
    }

    public fun <T> assertNotEquals(illegal: T, actual: T, message: String) {
        this.assertNotEquals(illegal, actual, Component.literal(message))
    }

    public infix fun <T> T.shouldEqual(expected: T) {
        assertEquals(expected, this)
    }

    /**
     * Infix form of [assertNotEquals], where the receiver is the actual value.
     */
    public infix fun <T> T.shouldNotEqual(illegal: T) {
        assertNotEquals(illegal, this)
    }

    public fun assertNull(value: Any?, message: Component? = null) {
        if (value != null) {
            this.fail(message ?: Component.literal("Expected null but was <$value>"))
        }
    }

    public fun assertNull(value: Any?, message: String) {
        this.assertNull(value, Component.literal(message))
    }

    public fun <T: Any> assertNotNull(value: T?, message: Component? = null): T {
        return value ?: this.fail(message ?: Component.literal("Expected value to not be null"))
    }

    public fun <T: Any> assertNotNull(value: T?, message: String): T {
        return this.assertNotNull(value, Component.literal(message))
    }

    /**
     * Asserts that [block] throws [T], returning the thrown exception.
     *
     * @param T the [Throwable] type that is expected to be thrown.
     * @param message The failing message.
     * @param block The code that should throw [T].
     */
    public suspend inline fun <reified T: Throwable> assertThrows(
        message: Component? = null,
        block: suspend () -> Unit
    ): T {
        try {
            block.invoke()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            if (e is T) {
                return e
            }
            this.fail(message ?: Component.literal(
                "Expected ${T::class.java.simpleName} but threw ${e::class.java.simpleName}"
            ))
        }
        this.fail(message ?: Component.literal("Expected ${T::class.java.simpleName} but nothing was thrown"))
    }

    public suspend inline fun <reified T: Throwable> assertThrows(
        message: String,
        block: suspend () -> Unit
    ): T {
        return this.assertThrows<T>(Component.literal(message), block)
    }

    /**
     * Suspends until [condition] holds, failing the test if it does not within [timeout].
     *
     * The condition is checked once per tick, so [timeout] must fit within the `maxTicks` of the
     * test's [net.fabricmc.fabric.api.gametest.v1.GameTest] annotation, otherwise the test times out
     * before this can report a failure.
     *
     * @param timeout The timeout of the assertion.
     * @param message The failing message.
     * @param condition The condition that should eventually be true.
     */
    public suspend fun assertEventually(
        timeout: MinecraftTimeDuration = 5.Seconds,
        message: Component? = null,
        condition: () -> Boolean
    ) {
        var remaining = timeout.ticks
        while (!condition.invoke()) {
            if (remaining <= 0) {
                this.fail(message ?: Component.literal("Condition was not met within $timeout"))
            }
            delay(1.Ticks)
            remaining -= 1
        }
    }

    public suspend fun assertEventually(
        timeout: MinecraftTimeDuration = 5.Seconds,
        message: String,
        condition: () -> Boolean
    ) {
        this.assertEventually(timeout, Component.literal(message), condition)
    }

    public suspend fun assertNever(
        duration: MinecraftTimeDuration = 5.Seconds,
        message: Component? = null,
        condition: () -> Boolean
    ) {
        var remaining = duration.ticks
        while (remaining > 0) {
            if (condition.invoke()) {
                this.fail(message ?: Component.literal("Condition was met within $duration"))
            }
            delay(1.Ticks)
            remaining -= 1
        }
    }

    public suspend fun assertNever(
        duration: MinecraftTimeDuration = 5.Seconds,
        message: String,
        condition: () -> Boolean
    ) {
        this.assertNever(duration, Component.literal(message), condition)
    }

    private fun cleanup() {
        for (player in this.players) {
            if (this.server.playerList.getPlayer(player.uuid) != null) {
                this.server.playerList.remove(player)
            }
        }
        this.players.clear()
    }

    private companion object {
        private val TEST_PLAYER_COUNTER = AtomicInteger()
    }
}
