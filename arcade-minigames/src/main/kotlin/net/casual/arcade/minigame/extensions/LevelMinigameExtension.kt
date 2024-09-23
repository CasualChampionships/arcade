package net.casual.arcade.minigame.extensions

import net.casual.arcade.extensions.Extension
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.server.level.ServerLevel

internal class LevelMinigameExtension(
    val level: ServerLevel
): Extension {
    private var minigame: Minigame<*>? = null

    internal fun getMinigame(): Minigame<*>? {
        return this.minigame
    }

    internal fun setMinigame(minigame: Minigame<*>) {
        if (this.minigame != null && this.minigame !== minigame) {
            ArcadeUtils.logger.warn("Level ${this.level.dimension().location()} has been assigned multiple minigames!")
        }
        this.minigame = minigame
    }

    internal fun removeMinigame(minigame: Minigame<*>) {
        if (this.minigame === minigame) {
            this.minigame = null
        }
    }
}