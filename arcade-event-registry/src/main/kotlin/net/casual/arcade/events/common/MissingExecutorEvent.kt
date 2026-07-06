/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.common

/**
 * This interface is used to represent all events that
 * may fire before the server/client has been fully initialized.
 *
 * This type is needed as [net.casual.arcade.events.GlobalEventHandler]
 * has logic in place to ensure that events are broadcasted on the correct
 * logical thread. If an event is fired before the logical client/server
 * has been created then their corresponding threads may not exist. This
 * type serves to indicate that the developer is aware that the event will
 * be broadcast in this state otherwise warnings will be output at runtime.
 */
public interface MissingExecutorEvent