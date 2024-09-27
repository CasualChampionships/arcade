package net.casual.arcade.events.registry

import com.google.common.collect.HashMultimap
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerProvider
import net.casual.arcade.events.core.Event
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.function.Consumer

public object RegistryEventHandler: EventListener<RegistryLoadedFromResourcesEvent<*>>, ListenerProvider {
    private val listeners = HashMultimap.create<ResourceKey<*>, EventListener<*>>()
    private var closed = false

    @JvmStatic
    public fun <T> register(
        key: ResourceKey<Registry<T>>,
        listener: EventListener<RegistryLoadedFromResourcesEvent<T>>
    ) {
        if (this.closed) {
            throw IllegalStateException("Tried to register Registry event too late!")
        }

        this.listeners.put(key, listener)
    }

    @Internal
    @JvmStatic
    public fun load() {
        GlobalEventHandler.addProvider(this)
    }

    @Internal
    @JvmStatic
    public fun unload() {
        GlobalEventHandler.removeProvider(this)
        this.listeners.clear()
        this.closed = true
    }

    @Internal
    override fun invoke(event: RegistryLoadedFromResourcesEvent<*>) {
        this.invokeListeners(event)
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
        val listeners = this.listeners.get(event.registry.key())
            .map { it as EventListener<RegistryLoadedFromResourcesEvent<T>> }
            .sorted()
        for (listener in listeners) {
            listener.invoke(event)
        }
    }
}