/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import kotlinx.coroutines.*
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus
import java.util.function.Function

private val scopes = Reference2ObjectOpenHashMap<MinecraftServer, CoroutineScope>()

public fun MinecraftServer.launch(block: suspend CoroutineScope.() -> Unit) {
    this.getCoroutineScope().launch(block = block)
}

public fun <T> MinecraftServer.async(block: suspend CoroutineScope.() -> T): Deferred<T> {
    return this.getCoroutineScope().async(block = block)
}

public fun MinecraftServer.getCoroutineScope(): CoroutineScope {
    return scopes.computeIfAbsent(this, Function { server ->
        CoroutineScope(server.asCoroutineDispatcher() + SupervisorJob())
    })
}

@ApiStatus.Internal
public object ServerCoroutineUtils {
    public fun stopServer(server: MinecraftServer) {
        scopes.remove(server)?.coroutineContext?.cancelChildren()
    }
}
