package net.casualuhc.arcade.events.core

import net.casualuhc.arcade.events.EventHandler

/**
 * This class represents any event which can be
 * broadcast via the [EventHandler]. This object
 * will be passed into a listener which can
 * then act upon the given event.
 *
 * @see CancellableEvent
 * @see EventHandler
 */
abstract class Event