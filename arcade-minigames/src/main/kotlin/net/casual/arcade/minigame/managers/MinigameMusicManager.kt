package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.events.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerRespawnEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.minigame.utils.MinigameMusicProvider
import net.casual.arcade.resources.event.PlayerPackSuccessEvent
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.impl.TimedSound
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class MinigameMusicManager(
    private val minigame: Minigame<*>
) {
    private val current = Object2ObjectOpenHashMap<UUID, PlayingSound>()
    private val interrupted = Object2ObjectOpenHashMap<UUID, PlayingSound>()

    private var running = false

    public var provider: MinigameMusicProvider = MinigameMusicProvider.EMPTY

    init {
        this.minigame.events.register<ServerTickEvent> { this.tick() }
        this.minigame.events.register<PlayerDimensionChangeEvent> { (player) -> this.interrupt(player) }
        this.minigame.events.register<PlayerRespawnEvent> { (player) -> this.interrupt(player) }
        this.minigame.events.register<PlayerPackSuccessEvent> { (player) -> this.interrupt(player) }
        this.minigame.events.register<MinigameRemovePlayerEvent> { (_, player) -> this.stop(player) }
        this.minigame.events.register<PlayerLeaveEvent> { (player) -> this.cancel(player) }
    }

    public fun start() {
        this.running = true
    }

    public fun stop() {
        this.running = false
        this.interrupted.putAll(this.current)
        this.current.clear()
    }

    private fun tick() {
        if (!this.running || this.provider === MinigameMusicProvider.EMPTY) {
            return
        }

        val now = System.currentTimeMillis()
        for (player in this.minigame.players) {
            val interrupted = this.interrupted.remove(player.uuid)
            if (interrupted != null) {
                val next = this.provider.resumeMusicFor(player, interrupted.timed)
                if (next != null) {
                    this.start(player, next)
                    continue
                }
            }

            val current = this.current[player.uuid]
            if (current == null) {
                val next = this.provider.getNextMusicFor(player, null)
                if (next != null) {
                    this.start(player, next)
                }
            } else if (current.end >= now) {
                val next = this.provider.getNextMusicFor(player, current.timed)
                if (next != null) {
                    this.start(player, next)
                } else {
                    this.current.remove(player.uuid)
                }
            }
        }
    }

    private fun start(player: ServerPlayer, timed: TimedSound) {
        val end = System.currentTimeMillis() + timed.duration.milliseconds
        this.current[player.uuid] = PlayingSound(timed, end)
        player.sendSound(timed.sound)
    }

    private fun interrupt(player: ServerPlayer) {
        val playing = this.current.remove(player.uuid)
        if (playing != null) {
            this.interrupted[player.uuid] = playing
        }
    }

    private fun stop(player: ServerPlayer) {
        val playing = this.cancel(player)
        if (playing != null) {
            val location = playing.timed.sound.event.location
            val source = playing.timed.sound.source
            player.connection.send(ClientboundStopSoundPacket(location, source))
        }
    }

    private fun cancel(player: ServerPlayer): PlayingSound? {
        this.interrupted.remove(player.uuid)
        return this.current.remove(player.uuid)
    }

    private class PlayingSound(val timed: TimedSound, val end: Long)
}