package net.casual.arcade.events

import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.core.Event
import java.util.function.Consumer

/**
 * An event handler that only listens for a single event type.
 *
 * Typically, this is used if you want to listen for a specific
 * event but not for the lifetime of your mod so registering it
 * with the [GlobalEventHandler] is not wanted.
 */
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
        /**
         * Creates a new [SingleEventHandler] for the given event type.
         *
         * @param T The type of event.
         * @param priority The priority of your event listener.
         * @param phase The phase of the event, [DEFAULT] by default.
         * @param listener The callback which will be invoked when the event is fired.
         * @return A new [SingleEventHandler] for the given event type.
         */
        public inline fun <reified T: Event> of(
            priority: Int = 1_000,
            phase: String = DEFAULT,
            listener: Consumer<T>
        ): SingleEventHandler<T> {
            return SingleEventHandler(T::class.java, EventListener.of(priority, phase, listener))
        }
    }
}