package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.Extension
import net.casual.arcade.gui.display.ArcadeNameDisplay
import net.minecraft.server.level.ServerPlayer

internal class PlayerNameScoreExtension(
    private val owner: ServerPlayer
): Extension {
    private var score: ArcadeNameDisplay? = null

    internal fun set(score: ArcadeNameDisplay) {
        val current = this.score
        if (current !== null) {
            current.removePlayer(this.owner)
        }
        this.score = score
    }

    internal fun remove() {
        this.score = null
    }

    internal fun disconnect() {
        this.score?.removePlayer(this.owner)
    }
}