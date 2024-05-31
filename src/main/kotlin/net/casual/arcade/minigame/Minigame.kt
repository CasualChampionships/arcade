package net.casual.arcade.minigame

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.casual.arcade.events.EventHandler
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.*
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.minigame.MinigameResources.Companion.removeFrom
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.MinigameResources.MultiMinigameResources
import net.casual.arcade.minigame.managers.*
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.phase.Phased
import net.casual.arcade.minigame.serialization.MinigameDataTracker
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.stats.ArcadeStats
import net.casual.arcade.task.SavableTask
import net.casual.arcade.utils.EventUtils.broadcast
import net.casual.arcade.utils.EventUtils.registerHandler
import net.casual.arcade.utils.EventUtils.unregisterHandler
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.toJsonObject
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import net.casual.arcade.utils.MinigameUtils
import net.casual.arcade.utils.MinigameUtils.minigame
import net.casual.arcade.utils.PlayerUtils.getKillCreditWith
import net.casual.arcade.utils.PlayerUtils.grantAdvancementSilently
import net.casual.arcade.utils.PlayerUtils.revokeAdvancement
import net.casual.arcade.utils.StatUtils.increment
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameRules
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.util.*

/**
 * This class represents a [Minigame] which player's can play.
 * This is the superclass of all minigames.
 *
 * This implements the bare-bones logic for a minigame and
 * has common utilities used in minigames.
 *
 * Each minigame has its own set of [GameSetting]s,
 * has its own [EventHandler], and own [TickedScheduler].
 * Minigames also provide a way to display the UI to all
 * the currently playing players, through [CustomBossBar]s,
 * [ArcadeSidebar], [ArcadePlayerListDisplay], and [ArcadeNameTag]s.
 *
 * The minigame keeps track of who is currently playing,
 * this can be accessed through [players].
 * It also keeps track of the [ServerLevel]s which are
 * part of the minigame.
 *
 * As well as the minigames own state, see: [setPhase], [paused].
 *
 * See more info about phases here: [Phase].
 *
 * You can implement your own minigame by extending this class:
 * ```kotlin
 * enum class MyMinigamePhase(
 *     override val id: String
 * ): Phase<MyMinigame> {
 *     Grace("grace"),
 *     Active("active"),
 *     DeathMatch("death_match")
 * }
 *
 * class MyMinigame(
 *     server: MinecraftServer
 * ): Minigame<MyMinigame>(server) {
 *     override val id = ResourceLocation("modid", "my_minigame")
 *
 *     override fun initialize() {
 *         super.initialize()
 *         this.levels.add(LevelUtils.overworld())
 *         this.events.register<MinigameAddPlayerEvent> { (_, player) ->
 *             player.sendSystemMessage(Component.literal("Welcome to My Minigame!"))
 *         }
 *     }
 *
 *     override fun getPhases(): List<Phase<MyMinigame>> {
 *         return MyMinigamePhase.values().toList()
 *     }
 * }
 * ```
 *
 * @param M The type of the child class.
 * @param server The [MinecraftServer] that created the [Minigame].
 * @see SavableMinigame
 * @see Phase
 */
@Suppress("JoinDeclarationAndAssignment")
public abstract class Minigame<M: Minigame<M>>(
    /**
     * The [MinecraftServer] that created the [Minigame].
     */
    public val server: MinecraftServer,
): Phased<M> {
    private var resources: MultiMinigameResources
    internal var initialized: Boolean

    internal val phases: List<Phase<M>>

    /**
     * This handles all the players for this minigame.
     */
    public val players: MinigamePlayerManager

    /**
     * This handles registering and invoking events.
     *
     * @see MinigameEventHandler
     */
    public val events: MinigameEventHandler<M>

    /**
     * This handles all the levels that will be used in the minigame.
     *
     * @see MinigameLevelManager
     */
    public val levels: MinigameLevelManager

    /**
     * The scheduler for scheduling tasks based on the minigames
     * ticking rate, the scheduler will be paused if the minigame
     * is paused.
     *
     * If your minigame is extending [SavableMinigame] then your
     * tasks can be saved, see [SavableTask] and [SavableMinigame].
     *
     * @see MinigameScheduler
     */
    public val scheduler: MinigameScheduler

    /**
     * This manages all the UI for the minigame.
     *
     * @see MinigameUIManager
     */
    public val ui: MinigameUIManager

    /**
     * This manager is for registering any minigame
     * specific commands, these commands will only be
     * accessible if a player is part of the minigame.
     *
     * @see MinigameCommandManager
     */
    public val commands: MinigameCommandManager

    /**
     * This manages minigame specific advancements.
     *
     * @see MinigameAdvancementManager
     */
    public val advancements: MinigameAdvancementManager

    /**
     * This manages minigame specific recipes.
     *
     * @see MinigameRecipeManager
     */
    public val recipes: MinigameRecipeManager

    /**
     * This manages certain effects for this minigame.
     *
     * @see MinigameEffectsManager
     */
    public val effects: MinigameEffectsManager

    /**
     * This manages minigame statistics.
     *
     * @see MinigameStatManager
     */
    public val stats: MinigameStatManager

    /**
     * This manages player minigame tags.
     *
     * @see MinigameTagManager
     */
    public val tags: MinigameTagManager

    /**
     * This manages a minigame's teams.
     *
     * @see MinigameStatManager
     */
    public val teams: MinigameTeamManager

    /**
     * This manages a minigame's chat.
     *
     * @see MinigameChatManager
     */
    public val chat: MinigameChatManager

    /**
     * This tracks minigame data which can be serialized
     * and then displayed to players later.
     *
     * @see MinigameDataTracker
     */
    public val data: MinigameDataTracker

    /**
     * This handles all the settings for a minigame.
     */
    public open val settings: MinigameSettings = MinigameSettings(this.cast())

    /**
     * The [UUID] of the minigame.
     */
    public var uuid: UUID
        internal set

    /**
     * What phase the minigame is currently in.
     * This, for example, may differ from whether there is a
     * grace period, death match, etc.
     *
     * @see setPhase
     * @see isPhase
     */
    public final override var phase: Phase<M>
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

    public var started: Boolean
        internal set

    public var closed: Boolean
        private set

    private var closing: Boolean

    public val ticking: Boolean
        get() = !this.paused && !this.isPhase(Phase.none())

    /**
     * The [ResourceLocation] of the [Minigame].
     */
    public abstract val id: ResourceLocation

    init {
        this.resources = MultiMinigameResources()
        this.initialized = false
        this.started = false
        this.closed = false
        this.closing = false

        this.phases = this.getAllPhases()

        this.uuid = UUID.randomUUID()

        this.scheduler = MinigameScheduler()

        val self = this.cast()
        // Events must be assigned first!
        this.events = MinigameEventHandler(self, MinigameEventHandler.Filterer(self))
        this.players = MinigamePlayerManager(self)
        this.levels = MinigameLevelManager(self)
        this.ui = MinigameUIManager(self)
        this.commands = MinigameCommandManager(self)
        this.advancements = MinigameAdvancementManager(self)
        this.recipes = MinigameRecipeManager(self)
        this.effects = MinigameEffectsManager(self)
        this.data = MinigameDataTracker(self)
        this.teams = MinigameTeamManager(self)
        this.chat = MinigameChatManager(self)
        this.tags = MinigameTagManager(self)
        this.stats = MinigameStatManager()

        this.phase = Phase.none()
        this.uptime = 0

        this.paused = false
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
    public fun setPhase(phase: Phase<M>) {
        if (this.isPhase(phase)) {
            return
        }
        if (!this.phases.contains(phase)) {
            throw IllegalArgumentException("Cannot set minigame '${this.id}' phase to ${phase.id}")
        }
        this.scheduler.phased.cancelAll()
        this.events.phasedHandler.clear()

        val self = this.cast()
        this.phase.end(self, phase)
        val previous = this.phase
        this.phase = phase
        this.phase.start(self, previous)
        this.phase.initialize(self)

        MinigameSetPhaseEvent(self, phase, previous).broadcast()
    }

    /**
     * This gets the [MinigameResources] for this minigame which
     * will be applied when the player joins this minigame.
     */
    public fun getResources(): MinigameResources {
        return this.resources
    }

    public fun addResources(resources: MinigameResources) {
        if (this.resources.addResources(resources)) {
            resources.sendTo(this.players)
        }
    }

    public fun removeResources(resources: MinigameResources) {
        if (this.resources.removeResources(resources)) {
            resources.removeFrom(this.players)
        }
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
            MinigamePauseEvent(this).broadcast()
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
            MinigameUnpauseEvent(this).broadcast()
        }
    }

    /**
     * This sets the [GameRules] for all the levels in the minigame.
     *
     * @param modifier The modifier to apply to the game rules.
     * @see GameRules
     */
    public fun setGameRules(modifier: GameRules.() -> Unit) {
        for (level in this.levels.all()) {
            modifier(level.gameRules)
        }
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

        MinigameCloseEvent(this).broadcast()
        for (player in this.players) {
            this.players.remove(player)
        }
        for (level in this.levels.all()) {
            level.minigame.removeMinigame()
        }
        // Closed is true after remove player
        this.closed = true
        if (this.settings.shouldDeleteLevels) {
            this.levels.deleteHandles()
        }
        this.levels.clear()
        this.events.unregisterHandler()
        this.events.minigameHandler.clear()
        this.events.phasedHandler.clear()
        this.scheduler.minigame.cancelAll()
        this.scheduler.phased.cancelAll()

        this.initialized = false
        this.phase = Phase.none()

        Minigames.unregister(this)
    }


    /**
     * This serializes some minigame information for debugging purposes.
     *
     * Implementations of minigame can add their own info with [appendAdditionalDebugInfo].
     *
     * @return The [JsonObject] containing the minigame's state.
     */
    public fun getDebugInfo(): JsonObject {
        val json = JsonObject()
        json.addProperty("minigame", this::class.java.simpleName)
        json.addProperty("initialized", this.initialized)
        json.addProperty("serializable", this is SavableMinigame)
        json.addProperty("uuid", this.uuid.toString())
        json.addProperty("id", this.id.toString())
        json.addProperty("uptime", this.uptime)
        json.add("players", this.players.all.toJsonStringArray { it.scoreboardName })
        json.add("offline_players", this.players.offlineProfiles.toJsonObject { it.name to JsonPrimitive(it.id?.toString()) })
        json.add("admins", this.players.admins.toJsonStringArray { it.scoreboardName })
        json.add("spectating", this.players.spectating.toJsonStringArray { it.scoreboardName })
        json.add("teams", this.teams.getAllTeams().toJsonStringArray { it.name })
        json.add("spies", this.chat.spies.toJsonStringArray { it.toString() })
        json.add("playing_teams", this.teams.getPlayingTeams().toJsonStringArray { it.name })
        json.add("eliminated_teams", this.teams.getEliminatedTeams().toJsonStringArray { it.name })
        json.add("levels", this.levels.all().toJsonStringArray { it.dimension().location().toString() })
        json.add("phases", this.phases.toJsonStringArray { it.id })
        json.addProperty("phase", this.phase.id)
        json.addProperty("ticking", this.ticking)
        json.addProperty("paused", this.paused)
        json.add("settings", this.settings.all().toJsonObject { it.name to it.serializeValue() })
        json.add("advancements", this.advancements.all().toJsonStringArray { it.id.toString() })
        json.add("recipes", this.recipes.all().toJsonStringArray { it.id.toString() })
        json.add("commands", this.commands.getAllRootCommands().toJsonStringArray { it })
        this.appendAdditionalDebugInfo(json)
        return json
    }

    /**
     * Gets the minigame's debug information as a string.
     *
     * @return The minigames debug information.
     */
    override fun toString(): String {
        return JsonUtils.GSON.toJson(this.getDebugInfo())
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

        MinigameStartEvent(this).broadcast()

        // The first phase is MinigamePhase.none()
        // This will never IOOB because we always have at least 2 phases
        this.setPhase(this.phases[1])
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
        MinigameCompleteEvent(this).broadcast()

        this.close()
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
            if (!this.initialized) {
                throw IllegalStateException("Failed to initialize minigame ${this.id}, you must call super.initialize()")
            }
        }
    }

    /**
     * This method initializes the core functionality of the
     * minigame, such as registering events.
     */
    protected open fun initialize() {
        this.registerEvents()
        this.events.registerHandler()
        this.levels.unregisterHandler()
        MinigameUtils.parseMinigameEvents(this)

        Minigames.register(this)

        this.initialized = true
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
    protected abstract fun getPhases(): Collection<Phase<M>>

    /**
     * This returns `this` object but cast to its implementation
     * type [M] to allow for calling methods that require
     * the implemented object.
     *
     * @return The cast `this`.
     */
    protected fun cast(): M {
        @Suppress("UNCHECKED_CAST")
        return this as M
    }

    /**
     * This appends any additional debug information to [getDebugInfo].
     *
     * @param json The json append to.
     */
    @OverrideOnly
    protected open fun appendAdditionalDebugInfo(json: JsonObject) {

    }

    private fun getAllPhases(): List<Phase<M>> {
        val phases = HashSet(this.getPhases())
        phases.add(Phase.none())
        phases.add(Phase.end())
        return phases.sortedWith { a, b -> a.compareTo(b) }
    }

    private fun registerEvents() {
        this.events.register<ServerTickEvent> { this.onServerTick(it) }
        this.events.register<PlayerTickEvent> { this.onPlayerTick(it) }
        this.events.register<PlayerJoinEvent> { this.onPlayerJoin(it) }
        this.events.register<PlayerDeathEvent> { this.onPlayerDeath(it) }
        this.events.register<PlayerDamageEvent>(Int.MAX_VALUE) { this.onPlayerDamage(it) }
        this.events.register<MinigameAddPlayerEvent>(-1000) { this.onPlayerAdd(it) }
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
        if (this.players.has(player)) {
            this.stats.getOrCreateStat(player, ArcadeStats.PLAY_TIME).increment()
        }
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
        val (player, _, source) = event
        val amount = event.resultOrElse { event.amount }
        this.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_TAKEN).increment(amount)

        val attacker = source.entity
        if (attacker is ServerPlayer && this.players.has(attacker)) {
            this.stats.getOrCreateStat(attacker, ArcadeStats.DAMAGE_DEALT).increment(amount)
        }
    }

    private fun onPlayerAdd(event: MinigameAddPlayerEvent) {
        this.getResources().sendTo(event.player)

        val advancements = this.data.getAdvancements(event.player)
        for (advancement in advancements) {
            event.player.grantAdvancementSilently(advancement)
        }
    }

    private fun onPlayerRemove(event: MinigameRemovePlayerEvent) {
        this.getResources().removeFrom(event.player)

        for (advancement in this.advancements.all()) {
            event.player.revokeAdvancement(advancement)
        }
    }

    private fun onServerStopping() {
        if (this.settings.pauseOnServerStop && !this.paused) {
            this.pause()
        }
    }
}