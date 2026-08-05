/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.*
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus.Internal
import org.slf4j.LoggerFactory
import java.util.*
import java.util.Queue
import java.util.function.Function
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

private val serverLogger = LoggerFactory.getLogger("ServerCoroutineScope")
private val clientLogger = LoggerFactory.getLogger("ClientCoroutineScope")

private val scopes = Reference2ObjectOpenHashMap<Any, CoroutineScope>()
private val delays = Reference2ObjectOpenHashMap<Any, Int2ObjectOpenHashMap<Queue<CompletableDeferred<Unit>>>>()
private val ticks = Reference2IntOpenHashMap<Any>()

public fun MinecraftServer.launch(block: suspend CoroutineScope.() -> Unit): Job {
    return this.getCoroutineScope().launch(block = block)
}

public fun <T> MinecraftServer.async(block: suspend CoroutineScope.() -> T): Deferred<T> {
    return this.getCoroutineScope().async(block = block)
}

public fun MinecraftServer.getCoroutineScope(): CoroutineScope {
    return scopes.computeIfAbsent(this, Function { _ ->
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            serverLogger.error("Uncaught exception while running server coroutine", throwable)
        }
        CoroutineScope(
            this.asCoroutineDispatcher() +
                MinecraftContext(this) +
                SupervisorJob() +
                CoroutineName("MinecraftServer") +
                exceptionHandler
        )
    })
}

public fun Minecraft.launch(block: suspend CoroutineScope.() -> Unit): Job {
    return this.getCoroutineScope().launch(block = block)
}

public fun <T> Minecraft.async(block: suspend CoroutineScope.() -> T): Deferred<T> {
    return this.getCoroutineScope().async(block = block)
}

public fun Minecraft.getCoroutineScope(): CoroutineScope {
    return scopes.computeIfAbsent(this, Function { _ ->
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            clientLogger.error("Uncaught exception while running client coroutine", throwable)
        }
        CoroutineScope(
            this.asCoroutineDispatcher() +
                MinecraftContext(this) +
                SupervisorJob() +
                CoroutineName("MinecraftClient") +
                exceptionHandler
        )
    })
}

public suspend fun delay(duration: MinecraftTimeDuration): Unit = coroutineScope cs@ {
    if (duration <= MinecraftTimeDuration.ZERO) {
        return@cs
    }

    val interceptor = coroutineContext[ContinuationInterceptor]
    if (interceptor is MinecraftSchedulerDelay) {
        return@cs suspendCancellableCoroutine { cont ->
            interceptor.scheduleResumeAfterDelay(duration, cont)
        }
    }

    val minecraft = coroutineContext[MinecraftContext]?.minecraft
    if (minecraft == null || interceptor !is ExecutorCoroutineDispatcher || interceptor.executor !== minecraft) {
        throw IllegalStateException(
            "delay(MinecraftTimeDuration) can only be called from a coroutine running on a " +
                "TickedScheduler or on the main Minecraft thread"
        )
    }

    val delays = delays.getOrPut(minecraft, ::Int2ObjectOpenHashMap)
    val global = ticks.getInt(minecraft) + duration.ticks
    val queue = delays.getOrPut(global) { ArrayDeque(1) }
    val deferred = CompletableDeferred<Unit>()
    queue.add(deferred)
    deferred.await()
}

private fun startTick(minecraft: Any) {
    ticks.addTo(minecraft, 1)
}

private fun tickDelays(minecraft: Any) {
    val delays = delays[minecraft] ?: return
    val queue = delays.remove(ticks.getInt(minecraft)) ?: return
    for (deferred in queue) {
        deferred.complete(Unit)
    }
}

private fun stop(minecraft: Any) {
    scopes.remove(minecraft)?.coroutineContext?.cancelChildren()
    delays.remove(minecraft)
    ticks.removeInt(minecraft)
}

@Internal
public object ServerCoroutineUtils {
    public fun onTickStart(server: MinecraftServer) {
        startTick(server)
    }

    public fun onTick(server: MinecraftServer) {
        tickDelays(server)
    }

    public fun onStop(server: MinecraftServer) {
        stop(server)
    }
}

@Internal
public object ClientCoroutineUtils {
    public fun onTickStart(client: Minecraft) {
        startTick(client)
    }

    public fun onTick(client: Minecraft) {
        tickDelays(client)
    }

    public fun onStop(client: Minecraft) {
        stop(client)
    }
}

private class MinecraftContext(val minecraft: Any): CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key: CoroutineContext.Key<MinecraftContext>
}
