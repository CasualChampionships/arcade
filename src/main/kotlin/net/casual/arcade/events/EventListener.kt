package net.casual.arcade.events

import net.casual.arcade.events.core.Event
import org.jetbrains.annotations.ApiStatus.NonExtendable
import java.util.function.Consumer

public fun interface EventListener<T: Event>: Comparable<EventListener<T>> {
    public val priority: Int
        get() = 1_000
    public val phase: String
        get() = BuiltInEventPhases.DEFAULT

    public fun invoke(event: T)

    @NonExtendable
    override operator fun compareTo(other: EventListener<T>): Int {
        return this.priority.compareTo(other.priority)
    }

    private class Impl<T: Event>(
        override val priority: Int,
        override val phase: String,
        private val listener: Consumer<T>
    ): EventListener<T> {
        override fun invoke(event: T) {
            this.listener.accept(event)
        }
    }

    public companion object {
        public fun <T: Event> of(
            priority: Int = 1_000,
            phase: String = BuiltInEventPhases.DEFAULT,
            listener: Consumer<T>
        ): EventListener<T> {
            return Impl(priority, phase, listener)
        }
    }
}