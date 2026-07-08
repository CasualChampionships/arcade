/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.threading

import net.casual.arcade.events.common.Event

/**
 * This represents an [Event] that is broadcasted off the
 * main thread. The [net.casual.arcade.events.GlobalEventHandler]
 * pushes all [Event]s that are off thread back onto the main
 * thread to prevent accidental concurrency issues.
 *
 * Marking your event as [AsyncEvent] indicates that you are
 * aware that the event may be broadcast off thread and
 * want the listeners to be invoked without pushing back
 * to the main thread.
 */
public interface AsyncEvent