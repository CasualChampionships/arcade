package net.casual.arcade.events

import net.casual.arcade.events.common.Event

/**
 * This interface allows you to add your own
 * [EventListener]s to the [GlobalEventHandler]
 * by using the [GlobalEventHandler.addProvider],
 * and [GlobalEventHandler.removeProvider] methods.
 *
 * This interface serves the purpose of being able
 * to provide [EventListener]s for a given [Event]
 * type.
 *
 * The implementation of this interface is [SimpleListenerRegistry]
 * where you can register multiple event types with
 * multiple [EventListener]s.
 *
 * Alternatively if you just want to register a
 * singular [EventListener] see [SingleListenerProvider].
 *
 * @see getListenersFor
 * @see SimpleListenerRegistry
 * @see GlobalEventHandler
 * @see SingleListenerProvider
 */
public interface ListenerProvider {
    /**
     * This method gets all the [EventListener]s for a given
     * [Event] type, given by [type].
     *
     * The listeners **must** be sorted by their priority.
     *
     * @param type The type of the [Event] to get listeners for.
     * @return The list of [EventListener]s for the given [type].
     */
    public fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>>
}