/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.player

import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerStopEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders.create
import net.casual.arcade.replay.recorder.rejoin.RejoinedReplayPlayer
import net.casual.arcade.replay.recorder.settings.RecorderSettings
import net.casual.arcade.replay.recorder.settings.SimpleRecorderSettings
import net.casual.arcade.util.mixins.network.ServerCommonPacketListenerAccessor
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.player.server
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * This object manages all [ReplayPlayerRecorder]s.
 */
public object ReplayPlayerRecorders {
    private val recorders = ConcurrentHashMap<UUID, EnumMap<ReplayFormat, ReplayPlayerRecorder>>()
    private val closing = ObjectOpenHashSet<ReplayPlayerRecorder>()

    /**
     * This creates a [ReplayPlayerRecorder] for a given [player].
     *
     * @param player The player you want to record.
     * @param directory The recording directory.
     * @param format The replay format to record in.
     * @param settings The recorder settings to use.
     * @return The created recorder.
     * @see create
     */
    @JvmStatic
    public fun create(
        player: ServerPlayer,
        directory: Path,
        format: ReplayFormat = ReplayFormat.ReplayMod,
        settings: RecorderSettings = SimpleRecorderSettings.DEFAULT
    ): ReplayPlayerRecorder {
        if (player is RejoinedReplayPlayer) {
            throw IllegalArgumentException("Cannot create a ReplayPlayerRecorder for a rejoining player")
        }
        val connection = (player.connection as ServerCommonPacketListenerAccessor).arcade_getConnection()
        return this.create(player.server, player.gameProfile, connection, directory, format, settings)
    }

    /**
     * This creates a [ReplayPlayerRecorder] for a given [profile].
     *
     * @param server The [MinecraftServer] instance.
     * @param profile The profile of the player you are going to record.
     * @param directory The recording directory.
     * @param format The replay format to record in.
     * @param settings The recorder settings to use.
     * @return The created recorder.
     * @see create
     */
    @JvmStatic
    public fun create(
        server: MinecraftServer,
        profile: GameProfile,
        connection: Connection,
        directory: Path,
        format: ReplayFormat = ReplayFormat.ReplayMod,
        settings: RecorderSettings = SimpleRecorderSettings.DEFAULT,
    ): ReplayPlayerRecorder {
        if (this.has(profile.id, format)) {
            throw IllegalArgumentException("ReplayPlayerRecorder for player '${profile.name}' already exists")
        }

        val recorder = ReplayPlayerRecorder(server, profile, settings, format, directory, connection)
        this.addRecorder(recorder)
        return recorder
    }

    /**
     * Checks whether a player [uuid] has a recorder with
     * a given [format].
     *
     * @param uuid The player uuid to check.
     * @return Whether the player has a recorder.
     */
    @JvmStatic
    public fun has(uuid: UUID, format: ReplayFormat): Boolean {
        return this.recorders[uuid]?.containsKey(format) ?: false
    }

    /**
     * Checks whether a player [uuid] has a recorder.
     *
     * @param uuid The player uuid to check.
     * @return Whether the player has a recorder.
     */
    @JvmStatic
    public fun has(uuid: UUID): Boolean {
        return !this.recorders[uuid].isNullOrEmpty()
    }

    /**
     * Checks whether a player has a recorder.
     *
     * @param player The player to check.
     * @return Whether the player has a recorder.
     */
    @JvmStatic
    public fun has(player: ServerPlayer): Boolean {
        return this.has(player.uuid)
    }

    /**
     * Gets the recorders for a given player.
     *
     * This call is *thread safe*.
     *
     * @param player The player to get the recorder for.
     * @return The recorders for the player.
     */
    @JvmStatic
    public fun get(player: ServerPlayer): Collection<ReplayPlayerRecorder> {
        return this.get(player.uuid)
    }

    /**
     * Gets the recorders for a given player's uuid.
     *
     * This call is *thread safe*.
     *
     * @param uuid The uuid of the player to get the recorder for.
     * @return The recorders for the player.
     */
    @JvmStatic
    public fun get(uuid: UUID): Collection<ReplayPlayerRecorder> {
        return this.recorders[uuid]?.values ?: listOf()
    }

    /**
     * Gets a collection of all the currently recording player recorders.
     *
     * This call is *thread safe*.
     *
     * @return A collection of all the player recorders.
     */
    @JvmStatic
    public fun recorders(): Collection<ReplayPlayerRecorder> {
        return this.recorders.values.flatMap { it.values }
    }

    /**
     * Gets a collection of all the currently closing player recorders.
     *
     * @return A collection of all the closing player recorders.
     */
    @JvmStatic
    public fun closing(): Collection<ReplayPlayerRecorder> {
        return ArrayList(this.closing)
    }

    @JvmStatic
    public fun record(packet: Packet<*>) {
        for (recorders in this.recorders.values) {
            recorders.values.forEach { recorder -> recorder.record(packet) }
        }
    }

    @JvmStatic
    public fun record(player: ServerPlayer, packet: Packet<*>) {
        for (recorder in this.get(player)) {
            recorder.record(packet)
        }
    }

    @JvmStatic
    public fun record(uuid: UUID, packet: Packet<*>) {
        for (recorder in this.get(uuid)) {
            recorder.record(packet)
        }
    }

    /**
     * This only records a given packet if the recorder has
     * been fully initialized.
     *
     * This is needed in cases where the player can be sent
     * packets off-thread during the recorder initialization
     * process, if a packet from the from phase gets sent
     * then that will break the entire replay.
     *
     * @param uuid The uuid of the player being recorded.
     * @param packet The packet to record.
     */
    @JvmStatic
    public fun recordIfInitialized(uuid: UUID, packet: Packet<*>) {
        for (recorder in this.get(uuid)) {
            if (recorder.isInitialized()) {
                recorder.record(packet)
            }
        }
    }

    @JvmStatic
    public fun stop(uuid: UUID) {
        for (recorder in this.get(uuid).toList()) {
            recorder.stop()
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerStopEvent>(phase = ServerStopEvent.PHASE_POST) {
            for (recorder in this.recorders()) {
                recorder.stop()
            }
        }
        GlobalEventHandler.Server.register<ServerTickEvent> {
            for (recorders in this.recorders.values) {
                recorders.values.forEach(ReplayPlayerRecorder::tick)
            }
        }
    }

    internal fun close(server: MinecraftServer, recorder: ReplayPlayerRecorder, future: CompletableFuture<Long>) {
        this.removeRecorder(recorder)
        this.closing.add(recorder)
        future.thenRunAsync({
            this.closing.remove(recorder)
        }, server)
    }

    private fun addRecorder(recorder: ReplayPlayerRecorder) {
        // We copy-on-write for these inner maps
        this.recorders.compute(recorder.recordingPlayerUUID) { _, map ->
            val copy = if (map == null) EnumUtils.mapOf() else EnumMap(map)
            require(copy.put(recorder.format, recorder) == null) { "Overwrote previous ReplayPlayerRecorder!" }
            copy
        }
    }

    private fun removeRecorder(recorder: ReplayPlayerRecorder) {
        val uuid = recorder.recordingPlayerUUID
        this.recorders.compute(uuid) { _, map ->
            when (map) {
                null -> null
                else -> {
                    val copy = EnumMap(map)
                    copy.remove(recorder.format)
                    if (copy.isEmpty()) null else copy
                }
            }
        }
    }
}