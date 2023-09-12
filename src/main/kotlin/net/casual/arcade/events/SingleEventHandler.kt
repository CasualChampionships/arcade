package net.casual.arcade.events

import net.casual.arcade.events.core.Event

public class SingleEventHandler<T: Event>(
    public val type: Class<T>,
    listener: EventListener<T>
): ListenerHandler {
    private val listener = listOf(listener)

    /**
     * This method gets all the [EventListener]s for a given
     * [Event] type, given by [type].
     *
     * @param type The type of the [Event] to get listeners for.
     * @return The list of [EventListener]s for the given [type].
     */
    override fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>> {
        if (type == this.type) {
            return this.listener
        }
        return emptyList()
    }

    public companion object {
        public fun <T: Event> of(type: Class<T>, listener: EventListener<T>): SingleEventHandler<T> {
            return SingleEventHandler(type, listener)
        }

        public inline fun <reified T: Event> of(listener: EventListener<T>): SingleEventHandler<T> {
            return of(T::class.java, listener)
        }
    }
}