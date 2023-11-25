package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.gui.display.ArcadeNameScoreDisplay
import net.minecraft.server.network.ServerGamePacketListenerImpl

internal class PlayerNameScoreExtension(
    owner: ServerGamePacketListenerImpl
): PlayerExtension(owner) {
    private var score: ArcadeNameScoreDisplay? = null

    internal fun set(score: ArcadeNameScoreDisplay) {
        val current = this.score
        if (current !== null) {
            current.removePlayer(this.player)
        }
        this.score = score
    }

    internal fun remove() {
        this.score = null
    }

    internal fun disconnect() {
        this.score?.removePlayer(this.player)
    }
}