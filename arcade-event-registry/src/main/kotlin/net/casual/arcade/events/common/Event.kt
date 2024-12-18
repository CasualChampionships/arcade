/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.common

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
public interface Event