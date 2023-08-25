package net.casual.arcade.events

import net.casual.arcade.events.core.Event

fun interface EventListener<T: Event>: Comparable<EventListener<T>> {
    val priority: Int
        get() = 1_000

    fun invoke(event: T)

    override operator fun compareTo(other: EventListener<T>): Int {
        return this.priority.compareTo(other.priority)
    }
}