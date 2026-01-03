/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder

import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.replay.recorder.packet.RecordablePayload
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.utils.DateTimeUtils.formatHHMMSS
import net.casual.arcade.utils.getDebugName
import net.casual.arcade.replay.ArcadeReplay
import net.casual.arcade.replay.events.ReplayRecorderDurationLimitEvent
import net.casual.arcade.replay.events.ReplayRecorderStartEvent
import net.casual.arcade.replay.events.ReplayRecorderStopEvent
import net.casual.arcade.replay.events.player.ReplayRecorderFileSizeLimitEvent
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.io.writer.ReplayWriter
import net.casual.arcade.replay.util.DebugPacketData
import net.casual.arcade.replay.util.FileUtils
import net.casual.arcade.replay.util.ReplayMarker
import net.casual.arcade.replay.util.ReplayOptimizerUtils
import net.casual.arcade.replay.recorder.settings.RecorderSettings
import net.casual.arcade.replay.recorder.settings.SimpleRecorderSettings.Companion.asSimple
import net.casual.arcade.replay.util.ReplayPacketUtils
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.component.DataComponents
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.configuration.ConfigurationProtocols
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundLoginPacket
import net.minecraft.network.protocol.game.GameProtocols
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket
import net.minecraft.network.protocol.login.LoginProtocols
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.decoration.ItemFrame
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.builder.StandardToStringStyle
import org.apache.commons.lang3.builder.ToStringBuilder
import org.jetbrains.annotations.ApiStatus.Internal
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.io.path.pathString
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * This is the abstract class representing a replay recorder.
 *
 * This class is responsible for starting, stopping, and saving
 * the replay files as well as recording all the packets.
 *
 * @param server The [MinecraftServer] instance.
 * @param profile The profile of the player being recorded.
 * @see ReplayPlayerRecorder
 * @see ReplayChunkRecorder
 */
@OptIn(ExperimentalTime::class)
public abstract class ReplayRecorder(
    public val server: MinecraftServer,
    public val profile: GameProfile,
    public val settings: RecorderSettings,
    public val format: ReplayFormat,
    protected val path: Path,
) {
    private val packets by lazy { Object2ObjectOpenHashMap<String, DebugPacketData>() }
    private val metaProviders = ArrayList<Consumer<JsonObject>>()

    private var protocol: ProtocolInfo<*> = LoginProtocols.CLIENTBOUND

    private lateinit var start: Instant

    private var accumulatedPausedTime = Duration.ZERO
    private var lastPausedTimestamp: Instant? = null

    private var currentRecordingLength = Duration.ZERO

    private var lastFileSizeCheckTimestamp: Instant? = null

    private var initialization = AtomicReference(InitializedState.Uninitialized)
    private var ignore = false

    internal var started = false
        private set

    @Suppress("LeakingThis")
    protected val writer: ReplayWriter = this.format.writer(this.path).invoke(this)

    /**
     * The directory at which all the temporary replay
     * files will be stored.
     * This also determines the final location of the replay file.
     */
    public val location: Path
        get() = this.writer.path

    /**
     * The number of markers the replay has recorded.
     */
    public val markers: Int
        get() = this.writer.markers

    /**
     * The [UUID] of the player the recording is of.
     */
    public val recordingPlayerUUID: UUID
        get() = this.profile.id

    /**
     * Whether the replay recorder has stopped and
     * is no longer recording any packets.
     */
    public val stopped: Boolean
        get() = this.writer.closed

    /**
     * Whether the recorder is currently paused
     */
    public val paused: Boolean
        get() = this.lastPausedTimestamp != null

    /**
     * The level that the replay recording is currently in.
     */
    public abstract val level: ServerLevel

    /**
     * The current position of the recorder.
     */
    public abstract val position: Vec3

    /**
     * The current rotation of the recorder.
     */
    public abstract val rotation: Vec2

    /**
     * This records an outgoing clientbound packet to the
     * replay file.
     *
     * This method will throw an exception if the recorder
     * has not started recording yet.
     *
     * This method **is** thread-safe; however, it should be noted
     * that any packet optimizations cannot be done if called off
     * the main thread, therefore only calling this method on
     * the main thread is preferable.
     *
     * @param outgoing The outgoing [Packet].
     */
    public open fun record(outgoing: Packet<*>) {
        if (!this.started) {
            throw IllegalStateException("Cannot record packets if recorder not started")
        }
        if (this.ignore || this.stopped) {
            return
        }
        val safe = this.server.isSameThread
        if (this.settings.debug && !safe) {
            ArcadeUtils.logger.warn("Trying to record packet off-thread ${outgoing.getDebugName()}")
        }

        if (ReplayOptimizerUtils.shouldIgnorePacket(this, outgoing)) {
            return
        }

        if (outgoing is ClientboundBundlePacket) {
            for (sub in outgoing.subPackets()) {
                this.record(sub)
            }
            return
        }
        if (!this.writer.canRecordPacket(outgoing) || !this.canRecordPacket(outgoing)) {
            return
        }

        val protocol = this.protocol
        val timestamp = this.getRecordingLength()
        this.currentRecordingLength = timestamp

        this.runPreAutomaticInitialization()
        this.writer.writePacket(outgoing, protocol, timestamp, !safe).thenApply { bytes ->
            if (this.settings.debug && bytes != null) {
                val type = outgoing.getDebugName()
                this.packets.getOrPut(type) { DebugPacketData(type, 0, 0) }.increment(bytes)
            }
        }
        this.runPostAutomaticInitialization(outgoing)

        this.checkRecordingStatus()
    }

    /**
     * This tries to start this replay recorder and returns
     * whether it was successful in doing so.
     *
     * @param mode Whether this is restarting a previous recording, [StartingMode.Start] by default.
     * @return `true` if the recording started successfully `false` otherwise.
     */
    public fun start(mode: StartingMode = StartingMode.Start): Boolean {
        if (!this.started && this.runManualInitialization()) {
            this.onStart(mode)
            return true
        }

        return false
    }

    /**
     * Tries to pause the recording for this recorder.
     *
     * This method may not be successful based on whether
     * the recorder is permitted to pause recording, see
     * [canPauseRecording].
     *
     * @param force Whether to force this recorder to pause.
     * @return Whether the recorder successfully paused.
     */
    @JvmOverloads
    public fun pause(force: Boolean = false): Boolean {
        if (!this.paused && (force || this.canPauseRecording())) {
            this.lastPausedTimestamp = Clock.System.now()
            this.writer.pause()
            return true
        }
        return false
    }

    /**
     * Tries to resume recording for this recorder.
     *
     * This method will only succeed if the recorder is paused.
     *
     * @return Whether the recorder successfully resumed.
     */
    public fun resume(): Boolean {
        if (this.paused) {
            this.accumulatedPausedTime += this.getCurrentPauseLength(Clock.System.now())
            this.lastPausedTimestamp = null
            this.writer.resume()
            return true
        }
        return false
    }

    /**
     * Stops the replay recorder and returns a future which will be completed
     * when the file has completed saving or closing.
     *
     * A failed future will be returned if the replay is already stopped.
     *
     * @param save Whether the recorded replay should be saved to disk, `true` by default.
     * @return A future which will be completed after the recording has finished saving or
     *     closing, this completes with the file size of the final compressed replay in bytes.
     */
    @JvmOverloads
    public fun stop(save: Boolean = true): CompletableFuture<Long> {
        if (this.stopped) {
            return CompletableFuture.failedFuture(IllegalStateException("Cannot stop replay after already stopped"))
        }

        if (this.settings.debug) {
            ArcadeUtils.logger.info("Replay ${this.getName()} Debug Packet Data:\n${this.getDebugPacketData()}")
        }

        // We only save if the player has actually logged in...
        val shouldSave = save && this.protocol.id() == ConnectionProtocol.PLAY
        val future = this.writer.close(this.currentRecordingLength, shouldSave)
        this.onClosing(future)

        GlobalEventHandler.Server.broadcast(ReplayRecorderStopEvent(this, future))

        return future
    }

    /**
     * Adds a marker to the replay file which can be viewed in ReplayMod.
     *
     * @param name The name of the marker, null for unnamed.
     * @param position The marked position.
     * @param rotation The marked rotation.
     * @param timestamp The timestamp of the marker (milliseconds).
     */
    public fun addMarker(
        name: String? = null,
        position: Vec3 = this.position,
        rotation: Vec2 = this.rotation,
        timestamp: Duration = this.getRecordingLength(),
        color: Int = 0xFF0000
    ) {
        this.addMarker(ReplayMarker(name, position, rotation, timestamp, color))
    }

    /**
     * Adds a marker to the replay file.
     *
     * @param marker The marker to add.
     */
    public fun addMarker(marker: ReplayMarker) {
        this.writer.writeMarker(marker)
    }

    /**
     * This gets the current recording length of the replay recording.
     * This is the length of the replay recording, which deducts
     * any time that was spent paused.
     *
     * @return The duration of the recording.
     */
    public fun getRecordingLength(): Duration {
        val now = Clock.System.now()
        val total = this.getTotalRecordingLength(now)
        return total - this.accumulatedPausedTime - this.getCurrentPauseLength(now)
    }

    /**
     * This gets the total time this recording has been recording for
     * in real-time, this does not account for pauses.
     *
     * @return The total real-time duration of the recording.
     */
    public fun getTotalRecordingLength(): Duration {
        return this.getTotalRecordingLength(Clock.System.now())
    }

    /**
     * This returns the raw (uncompressed) file size of the replay in bytes.
     *
     * @return The raw file size of the replay in bytes.
     */
    public fun getRawRecordingSize(): Long {
        return this.writer.getRawRecordingSize()
    }

    /**
     * Provides the status of the replay recorder as a formatted string.
     *
     * @return A future that will provide the status of the replay recorder.
     */
    public fun getStatus(): String {
        val builder = ToStringBuilder(this, StandardToStringStyle().apply {
            fieldSeparator = ", "
            fieldNameValueSeparator = " = "
            isUseClassName = false
            isUseIdentityHashCode = false
        })

        val length = this.getRecordingLength().formatHHMMSS()
        val totalLength = this.getTotalRecordingLength().formatHHMMSS()
        builder.append("name", this.getName())
        builder.append("recording_length", length)
        builder.append("total_recording_length", totalLength)

        this.appendToStatus(builder)

        builder.append("raw_size", FileUtils.formatSize(this.getRawRecordingSize()))
        return builder.toString()
    }

    /**
     * Whether this recorder should compress recorded voicechat data.
     *
     * @return Whether to compress voicechat data.
     */
    public fun shouldCompressVoicechat(): Boolean {
        // We simply disallow compression for ReplayMod, just ignore the setting if enabled
        return this.settings.compressVoiceChatData && this.format == ReplayFormat.Flashback
    }

    /**
     * Returns whether a given player should be hidden from the player tab list.
     *
     * @return Whether the player should be hidden
     */
    public open fun shouldHidePlayerFromTabList(player: ServerPlayer): Boolean {
        return false
    }

    /**
     * This allows you to add any additional metadata which will be
     * saved in the replay file.
     *
     * @param meta The JSON metadata map which can be mutated.
     */
    public open fun addMetadata(meta: JsonObject) {
        meta.addProperty("name", this.getName())
        meta.addProperty("location", this.location.pathString)
        meta.addProperty("epoch_time_ms", System.currentTimeMillis())
        meta.addProperty("accumulated_paused_time", this.accumulatedPausedTime.formatHHMMSS())

        val mods = JsonObject()
        for ((mod, version) in ArcadeReplay.getLoadedMods()) {
            mods.addProperty(mod, version)
        }
        meta.add("mods", mods)

        meta.add("settings", this.settings.asSimple().asJson())

        for (provider in this.metaProviders) {
            provider.accept(meta)
        }
    }

    /**
     * This allows you to inject additional metadata to the replay file.
     *
     * @param provider The metadata provider.
     */
    public fun addMetadataProvider(provider: Consumer<JsonObject>) {
        this.metaProviders.add(provider)
    }

    protected fun spawnPlayer(player: ServerPlayer, packets: Collection<Packet<*>>) {
        this.writer.writePlayer(player, packets)
    }

    /**
     * This appends any additional data to the status.
     *
     * @param builder The [ToStringBuilder] which is used to build the status.
     * @see getStatus
     */
    protected open fun appendToStatus(builder: ToStringBuilder) {

    }

    /**
     * This method tries to restart the replay recorder by creating
     * a new instance of itself.
     *
     * @return Whether it successfully restarted.
     */
    public abstract fun restart(): Boolean

    /**
     * This gets the name of the replay recording.
     *
     * @return The name of the replay recording.
     */
    public abstract fun getName(): String

    /**
     * This starts the replay recording, note this is **not** called
     * to start a replay if a player is being recorded from the login phase.
     *
     * This method should just simulate the player joining the server.
     */
    protected abstract fun initialize(): Boolean

    /**
     * This gets called when the replay is closing.
     *
     * @param future The future that will complete once the replay has closed.
     */
    protected abstract fun onClosing(future: CompletableFuture<Long>)

    /**
     * Whether the current recorder can pause recording.
     *
     * @return Whether it can be paused.
     */
    protected abstract fun canPauseRecording(): Boolean

    /**
     * Determines whether a given packet is able to be recorded.
     *
     * @param packet The packet that is going to be recorded.
     * @return Whether this recorded should record it.
     */
    protected open fun canRecordPacket(packet: Packet<*>): Boolean {
        if (packet is ClientboundCustomPayloadPacket) {
            val payload = packet.payload
            if (payload is RecordablePayload && !payload.shouldRecord(this)) {
                return false
            }
            // FIXME: Add distant horizons support?
            if (payload.type().id.namespace == "distant_horizons") {
                return false
            }
        }
        return true
    }

    /**
     * Sends all map data for the item frames in the world.
     *
     * @param filter Filter for any item frames that shouldn't be recorded.
     */
    protected fun sendItemFrameMapData(filter: (ItemFrame) -> Boolean = { true }) {
        val level = this.level
        val frames = level.getEntities(EntityType.ITEM_FRAME, ItemFrame::hasFramedMap)
        for (frame in frames) {
            if (filter.invoke(frame)) {
                val id = frame.item.get(DataComponents.MAP_ID) ?: continue
                val packet = ReplayPacketUtils.createMapPacket(id, level) ?: continue
                this.record(packet)
            }
        }
    }

    /**
     * Calling this ignores any packets that would've been
     * recorded by this recorder inside the [block] function.
     *
     * @param block The function to call while ignoring packets.
     */
    public fun ignore(block: () -> Unit) {
        val previous = this.ignore
        try {
            this.ignore = true
            block()
        } finally {
            this.ignore = previous
        }
    }

    /**
     * Fires when a replay has started/restarted.
     *
     * @param mode Whether the recording is being started or restarted.
     */
    @Internal
    @JvmOverloads
    public fun onStart(mode: StartingMode = StartingMode.Start) {
        GlobalEventHandler.Server.broadcast(ReplayRecorderStartEvent(this, mode))
    }

    @Internal
    public abstract fun takeSnapshot()

    @Internal
    public open fun tick() {
        if (this.initialization.get() == InitializedState.Initialized) {
            this.writer.tick()
        }
    }

    /**
     * This method formats all the debug packet data
     * into a string.
     *
     * @return The formatted debug packet data.
     */
    @Internal
    public fun getDebugPacketData(): String {
        return this.packets.values
            .sortedByDescending { it.size }
            .joinToString(separator = "\n", transform = DebugPacketData::format)
    }

    /**
     * This method should be called after the player that is being
     * recorded has logged in.
     * This will mark the replay recorder as being started and will
     * change the replay recording phase into `CONFIGURATION`.
     */
    @Internal
    public fun afterLogin() {
        if (!this.started) {
            this.started = true
            this.start = Clock.System.now()
        }

        this.protocol = LoginProtocols.CLIENTBOUND
        // We will not have recorded this, so we need to do it manually.
        this.record(ClientboundLoginFinishedPacket(this.profile))

        this.protocol = ConfigurationProtocols.CLIENTBOUND
    }

    /**
     * This method should be called after the player has finished
     * their configuration phase, and this will mark the player
     * as playing the game - actually in the Minecraft world.
     */
    @Internal
    public fun afterConfigure() {
        this.protocol = GameProtocols.CLIENTBOUND_TEMPLATE.bind(RegistryFriendlyByteBuf.decorator(this.server.registryAccess()))
    }

    private fun runPreAutomaticInitialization() {
        if (this.initialization.compareAndSet(InitializedState.Uninitialized, InitializedState.Automatic)) {
            this.writer.beginInitialization()
        }
    }

    private fun runPostAutomaticInitialization(packet: Packet<*>) {
        if (packet is ClientboundLoginPacket) {
            if (this.initialization.compareAndSet(InitializedState.Automatic, InitializedState.Initialized)) {
                this.writer.endInitialization()
            }
        }
    }

    private fun runManualInitialization(): Boolean {
        try {
            this.initialization.set(InitializedState.Manual)
            this.writer.beginInitialization()
            return this.initialize()
        } finally {
            this.writer.endInitialization()
            this.initialization.set(InitializedState.Initialized)
        }
    }

    private fun getCurrentPauseLength(now: Instant = Clock.System.now()): Duration {
        val timestamp = this.lastPausedTimestamp
        return if (timestamp != null) now - timestamp else Duration.ZERO
    }

    private fun getTotalRecordingLength(now: Instant = Clock.System.now()): Duration {
        return if (this.started) now - this.start else Duration.ZERO
    }

    private fun checkRecordingStatus() {
        val maxDuration = this.settings.limits.maxDuration
        if (maxDuration.isPositive()) {
            if (this.getRecordingLength() > maxDuration) {
                this.stop(true)
                GlobalEventHandler.Server.broadcast(ReplayRecorderDurationLimitEvent(this))
                if (this.settings.limits.restartAfterMaxDuration) {
                    this.restart()
                }
                return
            }
        }

        val now = Clock.System.now()
        val lastFileSizeCheckTimestamp = this.lastFileSizeCheckTimestamp
        if (lastFileSizeCheckTimestamp == null || lastFileSizeCheckTimestamp < now - 1.minutes) {
            this.lastFileSizeCheckTimestamp = now
            val maxFileSize = this.settings.limits.maxRawSize
            if (maxFileSize.bytes > 0) {
                if (this.getRawRecordingSize() > maxFileSize.bytes) {
                    this.stop(true)
                    GlobalEventHandler.Server.broadcast(ReplayRecorderFileSizeLimitEvent(this))
                    if (this.settings.limits.restartAfterMaxRawSize) {
                        this.restart()
                    }
                    return
                }
            }
        }
    }

    public enum class StartingMode {
        Start, Restart;

        public fun getContinuousVerb(): String {
            return when (this) {
                Start -> "Starting"
                Restart -> "Restarting"
            }
        }
    }

    private enum class InitializedState {
        Uninitialized,
        Automatic,
        Manual,
        Initialized
    }
}