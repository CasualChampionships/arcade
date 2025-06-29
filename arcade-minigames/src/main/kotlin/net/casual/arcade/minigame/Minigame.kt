/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.*
import net.casual.arcade.minigame.events.*
import net.casual.arcade.minigame.managers.*
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.serialization.MinigameDataTracker
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.serialization.MinigameSerializer
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.stats.ArcadeStats
import net.casual.arcade.minigame.stats.Stat.Companion.increment
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.minigame.utils.MinigameResources.Companion.removeFrom
import net.casual.arcade.minigame.utils.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.utils.MinigameResources.MultiMinigameResources
import net.casual.arcade.minigame.utils.MinigameUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.PlayerUtils.getKillCreditWith
import net.casual.arcade.utils.PlayerUtils.revokeAdvancement
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.TagValueInput
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.lang.reflect.ParameterizedType
import java.nio.file.Path
import java.util.*

/**
 * This class represents a [Minigame] which players can play.
 * This is the superclass of all minigames.
 *
 * This implements the bare-bones logic for a minigame and
 * has common utilities used in minigames.
 *
 * Each minigame has its own set of managers which manage
 * all the core functionality of the minigame, see the fields
 * of this class for more information.
 *
 * As well as the minigames own state, see: [setPhase], [paused].
 * See more info about phases here: [Phase].
 *
 * You can implement your own minigame by extending this class.
 *
 * @param server The [MinecraftServer] that created the [Minigame].
 * @see Phase
 */
@Suppress("LeakingThis")
public abstract class Minigame(
    /**
     * The [MinecraftServer] that created the [Minigame].
     */
    public val server: MinecraftServer,
    /**
     * The unique id for this minigame.
     */
    public val uuid: UUID
) {
    private val properties = Object2ObjectLinkedOpenHashMap<String, () -> JsonElement>()

    private var closing: Boolean = false

    internal val phases: List<Phase<Minigame>>

    internal val serialization = MinigameSerializer(this)

    /**
     * This handles registering and invoking events.
     *
     * @see MinigameEventHandler
     */
    public val events: MinigameEventHandler = MinigameEventHandler(this)

    /**
     * This handles all the players for this minigame.
     */
    public val players: MinigamePlayerManager = MinigamePlayerManager(this)

    /**
     * This handles all the levels that will be used in the minigame.
     *
     * @see MinigameLevelManager
     */
    public val levels: MinigameLevelManager = MinigameLevelManager(this)

    /**
     * The scheduler for scheduling tasks based on the minigames
     * ticking rate, the scheduler will be paused if the minigame
     * is paused.
     *
     * @see MinigameScheduler
     */
    public val scheduler: MinigameScheduler = MinigameScheduler()

    /**
     * This manages all the UI for the minigame.
     *
     * @see MinigameUIManager
     */
    public val ui: MinigameUIManager = MinigameUIManager(this)

    /**
     * The resource pack manager for packs the minigame requires players to download.
     *
     * @see MinigameResources
     */
    public val resources: MultiMinigameResources = MultiMinigameResources()

    /**
     * This manager is for registering any minigame
     * specific commands, these commands will only be
     * accessible if a player is part of the minigame.
     *
     * @see MinigameCommandManager
     */
    public val commands: MinigameCommandManager = MinigameCommandManager(this)

    /**
     * This manages minigame specific advancements.
     *
     * @see MinigameAdvancementManager
     */
    public val advancements: MinigameAdvancementManager = MinigameAdvancementManager(this)

    /**
     * This manages minigame specific recipes.
     *
     * @see MinigameRecipeManager
     */
    public val recipes: MinigameRecipeManager = MinigameRecipeManager(this)

    /**
     * This manages certain effects for this minigame.
     *
     * @see MinigameEffectsManager
     */
    public val effects: MinigameEffectsManager = MinigameEffectsManager(this)

    /**
     * This manages music for this minigame.
     *
     * @see MinigameMusicManager
     */
    public val music: MinigameMusicManager = MinigameMusicManager(this)

    /**
     * This manages minigame statistics.
     *
     * @see MinigameStatManager
     */
    public val stats: MinigameStatManager = MinigameStatManager()

    /**
     * This manages player minigame tags.
     *
     * @see MinigameTagManager
     */
    public val tags: MinigameTagManager = MinigameTagManager(this)

    /**
     * This manages a minigame's teams.
     *
     * @see MinigameStatManager
     */
    public val teams: MinigameTeamManager = MinigameTeamManager(this)

    /**
     * This manages a minigame's chat.
     *
     * @see MinigameChatManager
     */
    public val chat: MinigameChatManager = MinigameChatManager(this)

    /**
     * This tracks minigame data which can be serialized
     * and then displayed to players later.
     *
     * @see MinigameDataTracker
     */
    public val data: MinigameDataTracker = MinigameDataTracker(this)

    /**
     * This handles all the settings for a minigame.
     */
    public open val settings: MinigameSettings = MinigameSettings(this)

    /**
     * What phase the minigame is currently in.
     * This, for example, may differ from whether there is a
     * grace period, death match, etc.
     *
     * @see setPhase
     */
    public var phase: Phase<Minigame>
        internal set

    /**
     * How long the minigame has been up for.
     * This does not include time that the minigame was paused for.
     */
    public var uptime: Int
        internal set

    /**
     * When minigames are paused, none of the scheduled
     * tasks will execute until the minigame in unpaused.
     *
     * @see pause
     * @see unpause
     */
    public var paused: Boolean
        internal set

    /**
     * Whether the minigame has started.
     */
    public var started: Boolean
        internal set

    /**
     * Whether the minigame has initialized.
     */
    public var initialized: Boolean
        internal set

    /**
     * Whether the minigame is closed.
     */
    public var closed: Boolean
        private set

    /**
     * Whether the minigame is ticking.
     */
    public val ticking: Boolean
        get() = !this.paused && this.started

    /**
     * Whether the minigame is serializable.
     */
    public val serializable: Boolean
        get() = this.factory() != null

    /**
     * The [ResourceLocation] of the [Minigame].
     */
    public abstract val id: ResourceLocation

    init {
        this.initialized = false
        this.started = false
        this.closed = false

        this.phases = this.getAllPhases()

        this.phase = Phase.none()
        this.uptime = 0

        this.paused = false

        this.addDefaultProperties()
    }

    /**
     * Starts the minigame.
     */
    public fun start() {
        if (this.started) {
            return
        }
        this.started = true

        this.tryInitialize()

        this.data.start()

        GlobalEventHandler.Server.broadcast(MinigameStartEvent(this))

        // The first phase is MinigamePhase.none()
        // This will never IOOB because we always have at least 2 phases
        this.setPhase(this.phases[1])
    }

    /**
     * This tries to initialize the minigame
     * if it's not already initialized.
     */
    public fun tryInitialize() {
        if (this.closed) {
            throw IllegalStateException("Cannot initialize closed minigame ${this.id}")
        }
        if (!this.initialized) {
            this.initialize()
        }
    }

    /**
     * This sets the phase of the minigame.
     * It will only be set if the given phase is
     * **different** to the current phase and in
     * the [phases] set.
     *
     * All minigames will be able to be set to either
     * [Phase.none] or [Phase.end].
     * You can set the minigame's phase to [Phase.end]
     * to end the final implemented minigame phase.
     *
     * When a phase is set, all previously scheduled
     * phase tasks will be cleared and will no longer run.
     * Further, all the registered phase events will be
     * cleared and will no longer be invoked.
     *
     * After this the [MinigameSetPhaseEvent] is
     * broadcasted for listeners.
     *
     * @param phase The phase to set the minigame to.
     * @throws IllegalArgumentException If the [phase] is not in the [phases] set.
     */
    public fun setPhase(phase: Phase<out Minigame>, force: Boolean = false) {
        if (this.phase == phase && !force) {
            return
        }
        if (!this.phases.contains(phase)) {
            throw IllegalArgumentException("Cannot set minigame '${this.id}' phase to ${phase.id}")
        }
        this.scheduler.phased.cancelAll()

        // We already validated phases when we created them
        // if this.phases contains phase it must be valid...
        @Suppress("UNCHECKED_CAST")
        phase as Phase<Minigame>

        this.phase.end(this, phase)
        val previous = this.phase
        this.phase = phase
        this.phase.start(this, previous)
        this.phase.initialize(this)

        GlobalEventHandler.Server.broadcast(MinigameSetPhaseEvent(this, phase, previous))
    }

    /**
     * Gets the phase for a given phase [id] if it exists.
     *
     * @param id The phase id of the desired phase.
     * @return The phase, null if not found.
     */
    public fun getPhase(id: String): Phase<Minigame>? {
        return this.phases.find { it.id == id }
    }

    /**
     * This will pause the minigame, stopping the scheduler
     * from executing any more tasks.
     * This will also broadcast the [MinigamePauseEvent].
     *
     * @see paused
     */
    public fun pause() {
        if (!this.paused) {
            this.paused = true
            GlobalEventHandler.Server.broadcast(MinigamePauseEvent(this))
        }
    }

    /**
     * This will unpause the minigame, resuming the scheduler.
     * This will also broadcast the [MinigameUnpauseEvent].
     *
     * @see paused
     */
    public fun unpause() {
        if (this.paused) {
            this.paused = false
            GlobalEventHandler.Server.broadcast(MinigameUnpauseEvent(this))
        }
    }

    /**
     * Ends the minigame, the difference between [close] and
     * [complete] is that this is only called after the minigame
     * is that this is only called after the minigame is considered
     * to be in its finished state.
     *
     * This should **only** be called the minigame implementation
     * to signify when it has naturally ended.
     *
     * @see close
     */
    public fun complete() {
        GlobalEventHandler.Server.broadcast(MinigameCompleteEvent(this))

        this.close()
    }

    /**
     * This closes the minigame, all players are removed from the
     * minigame, all tasks are cleared, and all events are unregistered.
     *
     * This also broadcasts the [MinigameCloseEvent] **before** all the players
     * have been removed.
     *
     * After a minigame has been closed, no more players are permitted to join.
     *
     * If your minigame has finished naturally you should call [complete] instead.
     *
     * @see complete
     */
    public fun close() {
        if (this.closing || this.closed) {
            return
        }
        this.closing = true

        this.data.end()

        GlobalEventHandler.Server.broadcast(MinigameCloseEvent(this))
        this.players.close()
        this.levels.close()

        // Closed = true after players are removed
        this.closed = true

        GlobalEventHandler.Server.removeProvider(this.events)
        this.events.clear()

        this.scheduler.minigame.cancelAll()
        this.scheduler.phased.cancelAll()

        this.initialized = false
        this.phase = Phase.none()

        Minigames.unregister(this)
    }

    /**
     * Gets the minigame's debug information as a string.
     *
     * @return The minigames debug information.
     */
    override fun toString(): String {
        val json = JsonObject()
        for ((name, property) in this.properties) {
            json.add(name, property.invoke())
        }
        return JsonUtils.GSON.toJson(json)
    }

    /**
     * Gets the save path for this minigame.
     *
     * @return The path where the minigame is saved.
     */
    public fun getSavePath(): Path {
        return Minigames.getInstancesSavePath(this.server)
            .resolve("${this.id.namespace}.${this.id.path}")
            .resolve(this.uuid.toString())
    }

    /**
     * The [MinigameFactory] instance which is able to create
     * instances of `this` minigame.
     *
     * @return The minigame factory, which may be null.
     */
    protected open fun factory(): MinigameFactory? {
        return null
    }

    /**
     * Loads custom data for this minigame.
     *
     * @param data Any serialized [data] which was written in [save].
     */
    protected open fun load(data: JsonObject) {

    }

    /**
     * Saves custom data for this minigame.
     *
     * @param data The [JsonObject] to write to.
     */
    protected open fun save(data: JsonObject) {

    }

    /**
     * Sets a property for this minigame with a given [name].
     *
     * Used for debugging and inspecting this minigame.
     *
     * @param name The name of the property.
     * @param getter The property getter.
     */
    protected fun property(name: String, getter: () -> Any?) {
        this.properties[name] = { JsonUtils.encodeToElement(getter.invoke()) }
    }

    internal fun properties(): Collection<String> {
        return this.properties.keys
    }

    internal fun property(name: String): JsonElement {
        return this.properties[name]?.invoke() ?: JsonNull.INSTANCE
    }

    internal fun internalSave(): JsonObject {
        val data = JsonObject()
        this.save(data)
        return data
    }

    internal fun internalLoad(data: JsonObject) {
        this.load(data)
    }

    internal fun internalFactory(): MinigameFactory? {
        return this.factory()
    }

    /**
     * This method initializes the core functionality of the
     * minigame, such as registering events.
     */
    private fun initialize() {
        this.registerEvents()
        GlobalEventHandler.Server.addProvider(this.events)
        this.levels.initialize()
        MinigameUtils.parseMinigameEvents(this)

        Minigames.register(this)

        this.initialized = true

        GlobalEventHandler.Server.broadcast(MinigameInitializeEvent(this))
    }

    /**
     * This gets all the [Phase]s that this [Minigame]
     * allows.
     *
     * The phases **do not** have to be in order, any duplicates
     * will also be removed.
     * Further you do not need to include the default phases
     * ([Phase.none] and [Phase.end]), they'll
     * be included automatically.
     *
     * This method will only be invoked **once**; when the
     * minigame is initialized, the phases are then stored in
     * a collection for the rest of the minigames lifetime.
     *
     * @return A collection of all the valid phases the minigame can be in.
     */
    @OverrideOnly
    protected abstract fun phases(): Collection<Phase<out Minigame>>

    private fun getAllPhases(): List<Phase<Minigame>> {
        val phases = HashSet<Phase<Minigame>>()
        for (phase in this.phases()) {
            phases.add(this.validatePhase(phase))
        }
        phases.add(Phase.none())
        phases.add(Phase.end())
        return phases.sortedWith { a, b -> a.compareTo(b) }
    }

    private fun registerEvents() {
        this.events.register<ServerTickEvent> { this.onServerTick(it) }
        this.events.register<PlayerTickEvent> { this.onPlayerTick(it) }
        this.events.register<PlayerJoinEvent> { this.onPlayerJoin(it) }
        this.events.register<PlayerDeathEvent> { this.onPlayerDeath(it) }
        this.events.register<PlayerDamageEvent>(1_000, BuiltInEventPhases.POST) { this.onPlayerDamage(it) }
        this.events.register<PlayerHealEvent>(1_000, BuiltInEventPhases.POST) { this.onPlayerHeal(it) }
        this.events.register<MinigameAddPlayerEvent>(Int.MAX_VALUE) { this.onPlayerAdd(it) }
        this.events.register<MinigameRemovePlayerEvent>(2000) { this.onPlayerRemove(it) }
        this.events.register<ServerStoppingEvent> { this.onServerStopping() }
    }

    private fun onServerTick(event: ServerTickEvent) {
        this.ui.tick(event.server)
        if (this.ticking) {
            this.uptime++
            this.scheduler.tick()
        }
    }

    private fun onPlayerTick(event: PlayerTickEvent) {
        val (player) = event
        this.stats.getOrCreateStat(player, ArcadeStats.PLAY_TIME).increment()
    }

    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val (player) = event
        this.stats.getOrCreateStat(player, ArcadeStats.RELOGS).increment()
    }

    private fun onPlayerDeath(event: PlayerDeathEvent) {
        this.stats.getOrCreateStat(event.player, ArcadeStats.DEATHS).increment()

        val killer = event.player.getKillCreditWith(event.source)
        if (killer is ServerPlayer && this.players.has(killer)) {
            this.stats.getOrCreateStat(killer, ArcadeStats.KILLS).increment()
        }
    }

    private fun onPlayerDamage(event: PlayerDamageEvent) {
        val (player, source, amount) = event
        if (amount > 0 && amount < 3.4028235E37F) {
            this.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_TAKEN).increment(amount)

            val attacker = source.entity
            if (attacker is ServerPlayer && this.players.has(attacker)) {
                this.stats.getOrCreateStat(attacker, ArcadeStats.DAMAGE_DEALT).increment(amount)
            }
        }
    }

    private fun onPlayerHeal(event: PlayerHealEvent) {
        val (player, healAmount) = event
        this.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_HEALED).increment(healAmount)
    }

    private fun onPlayerAdd(event: MinigameAddPlayerEvent) {
        this.resources.sendTo(event.player)
    }

    private fun onPlayerRemove(event: MinigameRemovePlayerEvent) {
        this.resources.removeFrom(event.player)

        for (advancement in this.advancements.all()) {
            event.player.revokeAdvancement(advancement)
        }
    }

    private fun onServerStopping() {
        if (this.settings.pauseOnServerStop && !this.paused && this.started) {
            this.pause()
        }
    }

    private fun addDefaultProperties() {
        this.property("minigame") { this::class.java.simpleName }
        this.property("initialized") { this.initialized }
        this.property("serializable") { this.serializable }
        this.property("uuid") { this.uuid.toString() }
        this.property("id") { this.id.toString() }
        this.property("uptime") { this.uptime }
        this.property("players") { this.players.all.map { it.scoreboardName } }
        this.property("offline_players") { this.players.offlineProfiles.associate { it.name to it.id.toString() } }
        this.property("admins") { this.players.admins.map { it.scoreboardName } }
        this.property("spectating") { this.players.spectating.map { it.scoreboardName } }
        this.property("teams") { this.teams.getAllTeams().map { it.name } }
        this.property("spies") { this.chat.spies.map { it.toString() } }
        this.property("playing_teams") { this.teams.getPlayingTeams().map { it.name } }
        this.property("eliminated_teams") { this.teams.getEliminatedTeams().map { it.name } }
        this.property("levels") { this.levels.all().map { it.dimension().location().toString() } }
        this.property("phases") { this.phases.map { it.id } }
        this.property("phase") { this.phase.id }
        this.property("ticking") { this.ticking }
        this.property("paused") { this.paused }
        this.property("settings") { this.settings.all().associate { it.name to it.serializeValue() } }
        this.property("advancements") { this.advancements.all().map { it.id.toString() } }
        this.property("recipes") { this.recipes.all().map { it.id.toString() } }
        this.property("commands") { this.commands.getAllRootCommands().map { it } }
    }

    private fun validatePhase(phase: Phase<out Minigame>): Phase<Minigame> {
        val clazz = phase.javaClass.genericInterfaces.find {
            it is ParameterizedType && (it.rawType as? Class<*>) == Phase::class.java
        } as? ParameterizedType

        val typeArgument = clazz?.actualTypeArguments?.firstOrNull()
        if (typeArgument is Class<*>) {
            if (!typeArgument.isAssignableFrom(this::class.java)) {
                throw IllegalStateException("Phase ${phase.id} is not valid for minigame ${this.id}")
            }
        }
        @Suppress("UNCHECKED_CAST")
        return phase as Phase<Minigame>
    }
}