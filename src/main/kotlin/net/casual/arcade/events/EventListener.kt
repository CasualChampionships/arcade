package net.casual.arcade.events

import net.casual.arcade.events.core.Event
import org.jetbrains.annotations.ApiStatus.NonExtendable

fun interface EventListener<T: Event>: Comparable<EventListener<T>> {
    val priority: Int
        get() = 1_000

    fun invoke(event: T)

    @NonExtendable
    override operator fun compareTo(other: EventListener<T>): Int {
        return this.priority.compareTo(other.priority)
    }
}