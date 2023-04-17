package net.casualuhc.arcade.events

import net.casualuhc.arcade.events.core.Event

interface EventListener<T: Event>: Comparable<EventListener<T>> {
    val priority: Int
        get() = 1_000

    fun invoke(event: T)

    override operator fun compareTo(other: EventListener<T>): Int {
        return this.priority.compareTo(other.priority)
    }
}