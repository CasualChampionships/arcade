/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events

import net.casual.arcade.events.common.Event
import org.jetbrains.annotations.ApiStatus.NonExtendable
import java.util.function.Consumer

/**
 * This interface represents a listener for a given event.
 *
 * @param T The type of event.
 * @see Event
 */
public fun interface EventListener<T: Event>: Comparable<EventListener<T>> {
    /**
     * The priority of the event listener.
     *
     * This determines in what order the listener will be invoked.
     * Lower values of [priority] will result in being invoked earlier.
     */
    public val priority: Int
        get() = 1_000

    /**
     * The phase of the event listener.
     *
     * The phase depends on the event, and can be used to determine
     * when the listener is invoked, see the event implementation
     * you are listening to for more information.
     */
    public val phase: String
        get() = BuiltInEventPhases.DEFAULT

    /**
     * Whether the event listener is required to be executed on
     * the main thread.
     */
    public val requiresMainThread: Boolean
        get() = true

    public fun invoke(event: T)

    @NonExtendable
    override operator fun compareTo(other: EventListener<T>): Int {
        return this.priority.compareTo(other.priority)
    }

    private class Impl<T: Event>(
        override val priority: Int,
        override val phase: String,
        override val requiresMainThread: Boolean,
        private val listener: Consumer<T>
    ): EventListener<T> {
        override fun invoke(event: T) {
            this.listener.accept(event)
        }
    }

    public companion object {
        /**
         * Creates a new [EventListener] for the given event type.
         *
         * @param T The type of event.
         * @param priority The priority of your event listener.
         * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
         * @param listener The callback which will be invoked when the event is fired.
         * @return A new [EventListener] for the given event type.
         */
        public fun <T: Event> of(
            priority: Int = 1_000,
            phase: String = BuiltInEventPhases.DEFAULT,
            requiresMainThread: Boolean = true,
            listener: Consumer<T>
        ): EventListener<T> {
            return Impl(priority, phase, requiresMainThread, listener)
        }
    }
}