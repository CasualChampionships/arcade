package net.casual.arcade.events

import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.core.Event
import java.util.function.Consumer

/**
 * This interface lets you register event listeners.
 *
 * @see SimpleListenerRegistry
 */
public interface ListenerRegistry: ListenerProvider {
    /**
     * Registers an event listener with a given priority.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * The priority that you register the event with determines
     * in what order the listener will be invoked. Lower values
     * of [priority] will result in being invoked earlier.
     *
     * The phase depends on the event, and can be used to determine
     * when the listener is invoked, see the event implementation
     * you are listening to for more information.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param priority The priority of your event listener.
     * @param phase The phase of the event, [DEFAULT] by default.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: Event> register(type: Class<T>, priority: Int = 1_000, phase: String = DEFAULT, listener: Consumer<T>) {
        this.register(type, EventListener.of(priority, phase, listener))
    }

    /**
     * Registers an event listener.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public fun <T: Event> register(type: Class<T>, listener: EventListener<T>)
}

