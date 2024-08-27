package net.casual.arcade.minigame.exception

public class MinigameSerializationException: RuntimeException {
    public constructor(message: String): super(message)

    public constructor(message: String, cause: Throwable): super(message, cause)
}