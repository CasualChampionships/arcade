package net.casual.arcade.events

import net.casual.arcade.events.core.Event

interface ListenerHandler {
    fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>>
}