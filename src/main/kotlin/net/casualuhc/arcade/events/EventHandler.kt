package net.casualuhc.arcade.events

import net.casualuhc.arcade.events.core.Event
import java.util.LinkedList
import java.util.function.Consumer

object EventHandler {
    private val events = HashMap<Class<*>, ArrayList<Consumer<Event>>>()

    @JvmStatic
    fun broadcast(event: Event) {
        val listeners = this.events[event::class.java] ?: return
        var i = 0
        while (i < listeners.size) {
            listeners[i++].accept(event)
        }
    }

    inline fun <reified T: Event> register(listener: Consumer<T>) {
        this.register(T::class.java, listener)
    }

    @JvmStatic
    fun <T: Event> register(type: Class<T>, listener: Consumer<T>) {
        val listeners = this.events.getOrPut(type) { ArrayList() }
        @Suppress("UNCHECKED_CAST")
        listeners.add(listener as Consumer<Event>)
    }
}