/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.common

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.threading.AsyncEvent

/**
 * This class is the superclass of all event which can be
 * broadcast via the [GlobalEventHandler]. This object
 * will be passed into a listener which can
 * then act upon the given event.
 *
 * This class is sealed and is extended by [ClientSideEvent]
 * and [ServerSideEvent]. Custom events should extend from
 * these interfaces, which indicates on which logical
 * side the event should be broadcast from.
 *
 * There are also additional event types, although they
 * don't inherit [Event] they are intended to be used
 * on custom [Event] subtypes. See: [CancellableEvent],
 * [MissingExecutorEvent], [AsyncEvent].
 *
 * @see ClientSideEvent
 * @see ServerSideEvent
 * @see GlobalEventHandler
 */
public sealed interface Event