package net.casual.arcade.minigame.utils

import net.casual.arcade.utils.impl.TimedSound
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.server.level.ServerPlayer

public interface MinigameMusicProvider {
    public fun getNextMusicFor(player: ServerPlayer, previous: TimedSound?): TimedSound?

    public fun resumeMusicFor(
        player: ServerPlayer,
        interrupted: TimedSound,
        remaining: MinecraftTimeDuration
    ): TimedSound? {
        return interrupted
    }

    public companion object {
        public val EMPTY: MinigameMusicProvider = object: MinigameMusicProvider {
            override fun getNextMusicFor(player: ServerPlayer, previous: TimedSound?): TimedSound? {
                return null
            }
        }
    }
}