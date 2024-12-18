/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.exception

public class MinigameSerializationException: RuntimeException {
    public constructor(message: String): super(message)

    public constructor(message: String, cause: Throwable): super(message, cause)
}