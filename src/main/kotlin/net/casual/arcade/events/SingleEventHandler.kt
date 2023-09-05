package net.casual.arcade.events

import net.casual.arcade.events.core.Event

class SingleEventHandler<T: Event>(
    val type: Class<T>,
    listener: EventListener<T>
): ListenerHandler {
    private val listener = listOf(listener)

    override fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>> {
        if (type == this.type) {
            return this.listener
        }
        return emptyList()
    }

    companion object {
        fun <T: Event> of(type: Class<T>, listener: EventListener<T>): SingleEventHandler<T> {
            return SingleEventHandler(type, listener)
        }

        inline fun <reified T: Event> of(listener: EventListener<T>): SingleEventHandler<T> {
            return of(T::class.java, listener)
        }
    }
}