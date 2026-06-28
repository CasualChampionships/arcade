/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.*
import net.casual.arcade.utils.TimeUtils.Ticks
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
            interceptor.scheduleResumeAfterDelay(duration - 1.Ticks, cont)
        }
    }

    val context = coroutineContext[MinecraftContext]
        ?: throw IllegalStateException("Cannot run delay(MinecraftTimeDuration) on non-minecraft coroutine")
    val minecraft = context.minecraft
    val delays = delays.getOrPut(minecraft, ::Int2ObjectOpenHashMap)
    val global = ticks.getInt(minecraft) + duration.ticks - 1
    val queue = delays.getOrPut(global) { ArrayDeque(1) }
    val deferred = CompletableDeferred<Unit>()
    queue.add(deferred)
    deferred.await()
}

@Internal
public object ServerCoroutineUtils {
    public fun tickServer(server: MinecraftServer) {
        val delays = delays[server] ?: return
        val tick = ticks.addTo(server, 1)
        val queue = delays.remove(tick) ?: return
        for (deferred in queue) {
            deferred.complete(Unit)
        }
    }

    public fun stopServer(server: MinecraftServer) {
        scopes.remove(server)?.coroutineContext?.cancelChildren()
    }
}

@Internal
public object ClientCoroutineUtils {
    public fun tickClient(client: Minecraft) {
        val delays = delays[client] ?: return
        val tick = ticks.addTo(client, 1)
        val queue = delays.remove(tick) ?: return
        for (deferred in queue) {
            deferred.complete(Unit)
        }
    }

    public fun stopClient(client: Minecraft) {
        scopes.remove(client)?.coroutineContext?.cancelChildren()
    }
}

private class MinecraftContext(val minecraft: Any): CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key: CoroutineContext.Key<MinecraftContext>
}
