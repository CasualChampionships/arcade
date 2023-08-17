package net.casualuhc.arcade.minigame

import net.casualuhc.arcade.extensions.Extension
import net.minecraft.server.level.ServerPlayer

class PlayerMinigameExtension(
    private val owner: ServerPlayer
): Extension {
    private var minigame: Minigame? = null

    internal fun getMinigame(): Minigame? {
        return this.minigame
    }

    internal fun setMinigame(minigame: Minigame) {
        this.minigame?.removePlayer(this.owner)
        this.minigame = minigame
    }

    internal fun removeMinigame() {
        this.minigame = null
    }
}