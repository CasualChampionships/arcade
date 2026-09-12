/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import net.casual.arcade.gametest.utils.TestFakePlayer
import net.casual.arcade.gametest.utils.TestPlayerBuilder
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.getDebugName
import net.casual.arcade.utils.player.kick
import net.casual.arcade.utils.player.username
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestEntityBuilder
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.GameTestMobBuilder
import net.minecraft.gametest.framework.UnknownGameTestException
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import java.util.concurrent.atomic.AtomicInteger

/**
 * The receiver passed to every [TestSuite] test method.
 *
 * All state tracked here is per-test. Tests within a batch run *concurrently* in the same level.
 */
public class TestContext(public val helper: GameTestHelper) {
    private val closeables = ArrayList<AutoCloseable>()

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
     * Creates a builder for a [TestFakePlayer] with an offline profile who acts like a real player.
     *
     * The player is not created until [TestPlayerBuilder.spawn] is called.
     *
     * ```kotlin
     * val player = this.player(0, 1, 0).rotation(90.0F).spawn()
     * ```
     *
     * @return The [TestPlayerBuilder].
     */
    public fun player(): TestPlayerBuilder<TestFakePlayer> {
        return TestPlayerBuilder(this, ::TestFakePlayer)
    }

    /**
     * Creates a builder for a [TestFakePlayer] at the centre of the given
     * position, relative to the test structure.
     *
     * @return The [TestPlayerBuilder].
     */
    public fun player(x: Int, y: Int, z: Int): TestPlayerBuilder<TestFakePlayer> {
        return this.player().position(x, y, z)
    }

    public fun <E: Entity> entity(type: EntityType<E>, x: Int, y: Int, z: Int): GameTestEntityBuilder<E> {
        return this.helper.spawnEntity(type, x, y, z)
    }

    public fun <E: Mob> mob(type: EntityType<E>, x: Int, y: Int, z: Int): GameTestMobBuilder<E> {
        return this.helper.spawnMob(type, x, y, z)
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

    public inline fun <reified T: Packet<*>> TestFakePlayer.assertSent(predicate: (T) -> Boolean = { true }): T {
        return this.sent(predicate).firstOrNull()
            ?: fail("Expected ${T::class.java.simpleName} sent to ${this.username}, saw: ${this.packetsAsString()}")
    }

    public fun TestFakePlayer.assertSent(packet: Packet<*>) {
        if (!this.packets().contains(packet)) {
            fail("Expected ${packet.getDebugName()} sent to ${this.username}, saw: ${this.packetsAsString()}")
        }
    }

    public inline fun <reified T: Packet<*>> TestFakePlayer.assertNotSent(predicate: (T) -> Boolean = { true }) {
        val found = this.sent(predicate)
        if (found.isNotEmpty()) {
            fail("Expected no ${T::class.java.simpleName} sent to ${this.username}, saw ${this.packetsAsString()}")
        }
    }

    public fun TestFakePlayer.assertNotSent(packet: Packet<*>) {
        if (this.packets().contains(packet)) {
            fail("Expected no ${packet.getDebugName()} sent to ${this.username}, saw: ${this.packetsAsString()}")
        }
    }

    public fun track(closeable: AutoCloseable) {
        this.closeables.add(closeable)
    }

    internal fun track(player: TestFakePlayer) {
        this.track(AutoCloseable(player::kick))
    }

    private fun cleanup() {
        for (closeable in this.closeables.asReversed()) {
            try {
                closeable.close()
            } catch (e: Exception) {
                ArcadeUtils.logger.error("Failed to close a resource tracked by a test", e)
            }
        }
        this.closeables.clear()
    }

    public companion object {
        public fun nextTestPlayerName(): String {
            return "TestPlayer${TEST_PLAYER_COUNTER.incrementAndGet()}"
        }

        private val TEST_PLAYER_COUNTER = AtomicInteger()
    }
}
