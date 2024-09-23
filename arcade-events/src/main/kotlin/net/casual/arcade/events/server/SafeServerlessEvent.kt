package net.casual.arcade.events.server

import net.casual.arcade.events.core.Event

/**
 * This interface is used to represent all events that
 * may fire before the server has been fully initialized.
 */
public interface SafeServerlessEvent: Event