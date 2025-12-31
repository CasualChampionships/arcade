/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.scheduler.MinecraftSchedulerHolder
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus
import org.slf4j.LoggerFactory
import java.util.ArrayDeque
import java.util.Queue
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

private val logger = LoggerFactory.getLogger("ServerCoroutineScope")

private val scopes = Reference2ObjectOpenHashMap<MinecraftServer, CoroutineScope>()
private val delays = Reference2ObjectOpenHashMap<MinecraftServer, Int2ObjectOpenHashMap<Queue<CompletableDeferred<Unit>>>>()
private val ticks = Reference2IntOpenHashMap<MinecraftServer>()

public fun MinecraftServer.launch(block: suspend CoroutineScope.() -> Unit) {
    this.getCoroutineScope().launch(block = block)
}

public fun <T> MinecraftServer.async(block: suspend CoroutineScope.() -> T): Deferred<T> {
    return this.getCoroutineScope().async(block = block)
}

public fun MinecraftServer.getCoroutineScope(): CoroutineScope {
    return scopes.computeIfAbsent(this, Function { server ->
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            logger.error("Uncaught exception while running server coroutine", throwable)
        }
        CoroutineScope(
            server.asCoroutineDispatcher() +
                MinecraftServerContext(this) +
                SupervisorJob() +
                CoroutineName("MinecraftServer") +
                exceptionHandler
        )
    })
}

public suspend fun delay(duration: MinecraftTimeDuration): Unit = coroutineScope {
    if (duration <= MinecraftTimeDuration.ZERO) {
        return@coroutineScope
    }

    val interceptor = coroutineContext[ContinuationInterceptor]
    if (interceptor is MinecraftSchedulerHolder) {
        return@coroutineScope suspendCancellableCoroutine { cont ->
            interceptor.scheduler.schedule(duration - 1.Ticks) {
                cont.resume(Unit)
            }
        }
    }

    val context = coroutineContext[MinecraftServerContext]
        ?: throw IllegalStateException("Cannot run tickDelay on non-minecraft coroutine")
    val server = context.server
    val delays = delays.getOrPut(server, ::Int2ObjectOpenHashMap)
    val global = ticks.getInt(server) + duration.ticks - 1
    val queue = delays.getOrPut(global) { ArrayDeque(1) }
    val deferred = CompletableDeferred<Unit>()
    queue.add(deferred)
    deferred.await()
}

@ApiStatus.Internal
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

private class MinecraftServerContext(val server: MinecraftServer): CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key: CoroutineContext.Key<MinecraftServerContext>
}
