package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.events.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerRespawnEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.minigame.utils.MinigameMusicProvider
import net.casual.arcade.resources.event.PlayerPackSuccessEvent
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.TimedSound
import net.minecraft.core.Holder
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.Level
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.math.min

public class MinigameMusicManager(
    private val minigame: Minigame<*>
) {
    private val current = Object2ObjectOpenHashMap<UUID, PlayingSound>()
    private val interrupted = Object2ObjectOpenHashMap<UUID, PlayingSound>()

    private var suppressGameMusic = false
    private var running = false

    public var provider: MinigameMusicProvider = MinigameMusicProvider.EMPTY

    init {
        this.minigame.events.register<ServerTickEvent> { this.tick() }
        this.minigame.events.register<MinigameAddPlayerEvent> { (_, player) -> this.suppressGameMusic(player) }
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

    public fun suppressGameMusic(suppress: Boolean) {
        if (suppress && !this.suppressGameMusic) {
            this.suppressGameMusic()
        }
        this.suppressGameMusic = suppress
    }

    private fun tick() {
        if (this.suppressGameMusic && this.minigame.server.tickCount % 200 == 0) {
            this.suppressGameMusic()
        }

        if (!this.running || this.provider === MinigameMusicProvider.EMPTY) {
            return
        }

        val now = System.currentTimeMillis()
        for (player in this.minigame.players) {
            val interrupted = this.interrupted.remove(player.uuid)
            if (interrupted != null) {
                val remaining = min((interrupted.end - now) / 50, 0).toInt().Ticks
                val next = this.provider.resumeMusicFor(player, interrupted.timed, remaining)
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
            } else if (current.end < now) {
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
        val playing = this.stop(player)
        if (playing != null) {
            this.interrupted[player.uuid] = playing
        }
    }

    private fun stop(player: ServerPlayer): PlayingSound? {
        val playing = this.cancel(player)
        if (playing != null) {
            val location = playing.timed.sound.event.location
            val source = playing.timed.sound.source
            player.connection.send(ClientboundStopSoundPacket(location, source))
        }
        return playing
    }

    private fun cancel(player: ServerPlayer): PlayingSound? {
        this.interrupted.remove(player.uuid)
        return this.current.remove(player.uuid)
    }

    private fun suppressGameMusic(
        player: ServerPlayer,
        cached: MutableMap<ServerLevel, List<ClientboundStopSoundPacket>>
    ) {
        if (player.level().dimension() == Level.END) {
            player.stopMusic(SoundEvents.MUSIC_END)
            player.stopMusic(SoundEvents.MUSIC_DRAGON)
            return
        }
        if (player.level().dimension() != Level.NETHER && player.abilities.mayBuild && player.abilities.mayfly) {
            player.stopMusic(SoundEvents.MUSIC_CREATIVE)
            return
        }
        player.stopMusic(SoundEvents.MUSIC_GAME)
        player.stopMusic(SoundEvents.MUSIC_UNDER_WATER)

        // All the biome music
        val packets = cached.computeIfAbsent(player.serverLevel()) { level ->
            val biomes = level.chunkSource.generator.biomeSource.possibleBiomes()
            val packets = ArrayList<ClientboundStopSoundPacket>()
            for (biome in biomes) {
                val key = biome.value().backgroundMusic.flatMap { it.event.unwrapKey() }?.getOrNull() ?: continue
                packets.add(ClientboundStopSoundPacket(key.location(), SoundSource.MUSIC))
            }
            packets
        }
        for (packet in packets) {
            player.connection.send(packet)
        }
    }

    private fun suppressGameMusic(player: ServerPlayer) {
        if (this.suppressGameMusic) {
            this.suppressGameMusic(player, HashMap(1))
        }
    }

    private fun suppressGameMusic() {
        val cached = Reference2ObjectOpenHashMap<ServerLevel, List<ClientboundStopSoundPacket>>()
        for (player in this.minigame.players) {
            this.suppressGameMusic(player, cached)
        }
    }

    private fun ServerPlayer.stopMusic(holder: Holder.Reference<SoundEvent>) {
        this.connection.send(ClientboundStopSoundPacket(holder.key().location(), SoundSource.MUSIC))
    }

    private class PlayingSound(val timed: TimedSound, val end: Long)
}