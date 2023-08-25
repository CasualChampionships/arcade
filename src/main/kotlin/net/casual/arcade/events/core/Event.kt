package net.casual.arcade.events.core

import net.casual.arcade.events.GlobalEventHandler

/**
 * This class represents any event which can be
 * broadcast via the [GlobalEventHandler]. This object
 * will be passed into a listener which can
 * then act upon the given event.
 *
 * @see CancellableEvent
 * @see GlobalEventHandler
 */
interface Event