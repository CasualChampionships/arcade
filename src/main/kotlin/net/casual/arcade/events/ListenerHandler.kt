package net.casual.arcade.events

import net.casual.arcade.events.core.Event

/**
 * This interface allows you to add your own
 * [EventListener]s to the [GlobalEventHandler]
 * by using the [GlobalEventHandler.addHandler],
 * and [GlobalEventHandler.removeHandler] methods.
 *
 * This interface serves the purpose of being able
 * to provide [EventListener]s for a given [Event]
 * type.
 *
 * The implementation of this interface is [EventHandler]
 * where you can register multiple event types with
 * multiple [EventListener]s.
 *
 * Alternatively if you just want to register a
 * singular [EventListener] see [SingleEventHandler].
 *
 * @see getListenersFor
 * @see EventHandler
 * @see GlobalEventHandler
 * @see SingleEventHandler
 */
interface ListenerHandler {
    /**
     * This method gets all the [EventListener]s for a given
     * [Event] type, given by [type].
     *
     * @param type The type of the [Event] to get listeners for.
     * @return The list of [EventListener]s for the given [type].
     */
    fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>>
}