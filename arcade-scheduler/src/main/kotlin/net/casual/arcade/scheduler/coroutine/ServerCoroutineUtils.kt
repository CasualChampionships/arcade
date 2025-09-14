package net.casual.arcade.scheduler.coroutine

import it.unimi.dsi.fastutil.objects.Reference2ObjectFunction
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.*
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerStopEvent
import net.minecraft.server.MinecraftServer
import java.util.function.Function

private val dispatchers = Reference2ObjectOpenHashMap<MinecraftServer, CoroutineScope>()

public fun MinecraftServer.launch(block: suspend CoroutineScope.() -> Unit) {
    val dispatcher = dispatchers.computeIfAbsent(this, Function { server ->
        CoroutineScope(server.asCoroutineDispatcher() + SupervisorJob())
    })
    dispatcher.launch(block = block)
}

public fun <T> MinecraftServer.async(block: suspend CoroutineScope.() -> T): Deferred<T> {
    val dispatcher = dispatchers.computeIfAbsent(this, Function { server ->
        CoroutineScope(server.asCoroutineDispatcher() + SupervisorJob())
    })
    return dispatcher.async(block = block)
}

internal object ServerCoroutineUtils {
    fun registerEvents() {
        GlobalEventHandler.Server.register<ServerStopEvent> { (server) ->
            dispatchers.remove(server)?.coroutineContext?.cancelChildren()
        }
    }
}
