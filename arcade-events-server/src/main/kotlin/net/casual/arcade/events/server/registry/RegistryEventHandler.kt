/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.registry

import com.google.common.collect.HashMultimap
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerProvider
import net.casual.arcade.events.common.Event
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import org.jetbrains.annotations.ApiStatus.Internal

public object RegistryEventHandler: EventListener<RegistryLoadedFromResourcesEvent<*>>, ListenerProvider {
    private val listeners = HashMultimap.create<ResourceKey<*>, EventListener<*>>()
    private var closed = false

    @JvmStatic
    public fun <T> register(
        key: ResourceKey<Registry<T>>,
        listener: EventListener<RegistryLoadedFromResourcesEvent<T>>
    ) {
        if (closed) {
            throw IllegalStateException("Tried to register Registry event too late!")
        }

        listeners.put(key, listener)
    }

    @Internal
    @JvmStatic
    public fun load() {
        GlobalEventHandler.Server.addProvider(this)
        closed = false
    }

    @Internal
    @JvmStatic
    public fun unload() {
        GlobalEventHandler.Server.removeProvider(this)
        closed = true
    }

    @Internal
    override fun invoke(event: RegistryLoadedFromResourcesEvent<*>) {
        invokeListeners(event)
    }

    @Internal
    public override fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>> {
        if (type == RegistryLoadedFromResourcesEvent::class.java) {
            return listOf(this)
        }
        return emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> invokeListeners(event: RegistryLoadedFromResourcesEvent<T>) {
        val listeners = listeners.get(event.registry.key())
            .map { it as EventListener<RegistryLoadedFromResourcesEvent<T>> }
            .sorted()
        for (listener in listeners) {
            listener.invoke(event)
        }
    }
}