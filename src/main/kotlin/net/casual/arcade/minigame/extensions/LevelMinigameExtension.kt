package net.casual.arcade.minigame.extensions

import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerLevel

internal class LevelMinigameExtension(
    val level: ServerLevel
): Extension {
    private var minigame: Minigame<*>? = null

    internal fun getMinigame(): Minigame<*>? {
        return this.minigame
    }

    internal fun setMinigame(minigame: Minigame<*>) {
        if (this.minigame != null) {
            Arcade.logger.warn("Level ${this.level.dimension().location()} has been assigned multiple minigames!")
        }
        this.minigame = minigame
    }

    internal fun removeMinigame() {
        this.minigame = null
    }
}