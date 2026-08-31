/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events

/**
 * A handle to one or more registered [EventListener]s,
 * returned by [ListenerRegistry.register].
 *
 * @see ListenerRegistry
 */
public fun interface EventListenerHandle {
    public fun remove()

    public companion object {
        public val EMPTY: EventListenerHandle = EventListenerHandle { }

        public fun of(handles: List<EventListenerHandle>): EventListenerHandle {
            if (handles.isEmpty()) {
                return EMPTY
            }
            if (handles.size == 1) {
                return handles[0]
            }
            val copy = handles.toList()
            return EventListenerHandle {
                for (handle in copy) {
                    handle.remove()
                }
            }
        }
    }
}
