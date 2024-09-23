package net.casual.arcade.minigame.exception

public class MinigameCreationException: RuntimeException {
    public constructor(message: String): super(message)

    public constructor(message: String, cause: Throwable): super(message, cause)
}