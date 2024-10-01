package net.casual.arcade.minigame.utils

import net.casual.arcade.utils.impl.TimedSound
import net.minecraft.server.level.ServerPlayer

public interface MinigameMusicProvider {
    public fun getNextMusicFor(player: ServerPlayer, previous: TimedSound?): TimedSound?

    public fun resumeMusicFor(player: ServerPlayer, interrupted: TimedSound): TimedSound? {
        return this.getNextMusicFor(player, interrupted)
    }

    public companion object {
        public val EMPTY: MinigameMusicProvider = object: MinigameMusicProvider {
            override fun getNextMusicFor(player: ServerPlayer, previous: TimedSound?): TimedSound? {
                return null
            }
        }
    }
}