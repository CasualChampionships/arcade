package net.casual.arcade.events.server

import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.core.Event

/**
 * This interface marks an event as being intended to
 * be broadcast **off** the main thread, the broadcaster
 * will force the event to then be broadcast on the main
 * thread instead.
 *
 * It is important to note that if a [CancellableEvent] is
 * broadcast off thread then it cannot be cancelled.
 *
 * @see Event
 */
public interface ServerOffThreadEvent: Event