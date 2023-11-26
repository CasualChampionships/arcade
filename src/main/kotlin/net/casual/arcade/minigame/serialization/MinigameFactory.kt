package net.casual.arcade.minigame.serialization

import net.casual.arcade.minigame.Minigame

public fun interface MinigameFactory {
    public fun create(context: MinigameCreationContext): Minigame<*>
}