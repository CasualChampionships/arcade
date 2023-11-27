package net.casual.arcade.minigame

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.authlib.GameProfile
import net.casual.arcade.Arcade
import net.casual.arcade.events.EventHandler
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.PlayerDamageEvent
import net.casual.arcade.events.player.PlayerDeathEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.annotation.MinigameEvent
import net.casual.arcade.minigame.managers.*
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.settings.DisplayableGameSetting
import net.casual.arcade.settings.DisplayableGameSettingBuilder
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
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.MinigameUtils.minigame
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.ScreenUtils.DefaultMinigameSettingsComponent
import net.casual.arcade.utils.StatUtils.increment
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.MenuProvider
import net.minecraft.world.level.GameRules
import net.minecraft.world.scores.PlayerTeam
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.collections.set

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
 * [ArcadeSidebar], [ArcadeTabDisplay], and [ArcadeNameTag]s.
 *
 * The minigame keeps track of who is currently playing,
 * this can be accessed through [getPlayers].
 * It also keeps track of the [ServerLevel]s which are
 * part of the minigame.
 *
 * As well as the minigames own state, see: [setPhase], [paused].
 *
 * See more info about phases here: [MinigamePhase].
 *
 * You can implement your own minigame by extending this class:
 * ```kotlin
 * enum class MyMinigamePhase(
 *     override val id: String
 * ): MinigamePhase<MyMinigame> {
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
 *     val settings = Settings()
 *
 *     init {
 *         this.initialise()
 *     }
 *
 *     override fun initialise() {
 *         super.initialise()
 *         this.events.register<MinigameAddPlayerEvent> { (_, player) ->
 *             player.sendSystemMessage(Component.literal("Welcome to My Minigame!"))
 *         }
 *     }
 *
 *     override fun start() {
 *         this.setPhase(MyMinigamePhase.Grace)
 *     }
 *
 *     override fun getPhases(): List<MinigamePhase<MyMinigame>> {
 *         return MyMinigamePhase.values().toList()
 *     }
 *
 *     override fun getLevels(): Collection<ServerLevel> {
 *         return listOf(LevelUtils.overworld())
 *     }
 *
 *     inner class Settings {
 *         val mySetting by registerSetting(
 *             DisplayableGameSettingBuilder.boolean()
 *                 .name("my_setting")
 *                 .display(ItemStack(Items.GLOWSTONE).literalNamed("My Setting"))
 *                 .defaultOptions()
 *                 .value(true)
 *                 .build()
 *         )
 *     }
 * }
 * ```
 *
 * @param M The type of the child class.
 * @param server The [MinecraftServer] that created the [Minigame].
 * @see SavableMinigame
 * @see MinigamePhase
 */
public abstract class Minigame<M: Minigame<M>>(
    /**
     * The [MinecraftServer] that created the [Minigame].
     */
    public val server: MinecraftServer,
) {
    private val levelHandles: MutableSet<RuntimeWorldHandle>
    private val connections: MutableSet<ServerGamePacketListenerImpl>

    private var initialized: Boolean

    internal val offline: MutableSet<GameProfile>
    internal val gameSettings: MutableMap<String, DisplayableGameSetting<*>>
    internal val phases: List<MinigamePhase<M>>

    /**
     * This handles registering and invoking events.
     *
     * @see MinigameEventHandler
     */
    public val events: MinigameEventHandler<M>

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
     * This manages minigame statistics.
     *
     * @see MinigameStatManager
     */
    public val stats: MinigameStatManager

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
    public var phase: MinigamePhase<M>
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
     * The [ResourceLocation] of the [Minigame].
     */
    public abstract val id: ResourceLocation

    init {
        this.levelHandles = LinkedHashSet()
        this.connections = LinkedHashSet()

        this.initialized = false

        this.offline = LinkedHashSet()

        this.gameSettings = LinkedHashMap()
        this.phases = this.getAllPhases()

        this.uuid = UUID.randomUUID()

        this.scheduler = MinigameScheduler()
        this.events = MinigameEventHandler(this.cast())
        this.ui = MinigameUIManager(this.cast())
        this.commands = MinigameCommandManager(this.cast())
        this.advancements = MinigameAdvancementManager(this.cast())
        this.recipes = MinigameRecipeManager(this.cast())
        this.stats = MinigameStatManager()

        this.phase = MinigamePhase.none()

        this.paused = false
    }

    /**
     * Checks whether the minigame is in a given phase.
     *
     * @param phase The phase to check whether the minigame is in.
     * @return Whether the minigame is in that phase.
     */
    public fun isPhase(phase: MinigamePhase<M>): Boolean {
        return this.phase == phase
    }

    /**
     * Checks whether the minigame is before a given phase.
     *
     * @param phase The phase to check whether the minigame is before.
     * @return Whether the minigame is before that phase.
     */
    public fun isBeforePhase(phase: MinigamePhase<M>): Boolean {
        return this.phase < phase
    }

    /**
     * Checks whether the minigame is past a given phase.
     *
     * @param phase The phase to check whether the minigame has past.
     * @return Whether the minigame is past that phase.
     */
    public fun isAfterPhase(phase: MinigamePhase<M>): Boolean {
        return this.phase > phase
    }

    /**
     * This sets the phase of the minigame.
     * It will only be set if the given phase is
     * **different** to the current phase and in
     * the [phases] set.
     *
     * All minigames will be able to be set to either
     * [MinigamePhase.none] or [MinigamePhase.end].
     * You can set the minigame's phase to [MinigamePhase.end]
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
    public fun setPhase(phase: MinigamePhase<M>) {
        if (this.isPhase(phase)) {
            return
        }
        if (!this.phases.contains(phase)) {
            throw IllegalArgumentException("Cannot set minigame '${this.id}' phase to ${phase.id}")
        }
        this.scheduler.phased.cancelAll()
        this.events.phased.clear()

        val self = this.cast()
        this.phase.end(self)
        this.phase = phase
        this.phase.start(self)
        this.phase.initialize(self)

        MinigameSetPhaseEvent(self, phase).broadcast()
    }

    /**
     * This adds a player to the minigame.
     * The player may be rejected from joining the minigame.
     * The minigame must be [initialized], and must not yet
     * be tracking the given player.
     * The player may also be rejected by the [MinigameAddNewPlayerEvent].
     *
     * If the player is accepted this method will return `true`.
     *
     * If the player previously logged out (or the server restarted),
     * the player will automatically rejoin the minigame and the
     * [MinigameAddExistingPlayerEvent] will be broadcast instead.
     *
     * In both cases of the player joining a singular other event
     * will be broadcasted [MinigameAddPlayerEvent].
     *
     * @param player The player to add to the minigame.
     * @return Whether the player was successfully accepted.
     */
    public fun addPlayer(player: ServerPlayer): Boolean {
        if (!this.initialized || this.hasPlayer(player.uuid)) {
            return false
        }
        val hasMinigame = player.getMinigame() === this
        if (this.offline.remove(player.gameProfile) || hasMinigame) {
            if (!hasMinigame) {
                Arcade.logger.warn("Player's minigame UUID didn't work?!")
                player.minigame.setMinigame(this)
            }

            this.connections.add(player.connection)
            MinigameAddExistingPlayerEvent(this, player).broadcast()
            MinigameAddPlayerEvent(this, player).broadcast()
            return true
        }

        this.connections.add(player.connection)
        val event = MinigameAddNewPlayerEvent(this, player).broadcast()
        if (!event.isCancelled()) {
            player.minigame.setMinigame(this)
            MinigameAddPlayerEvent(this, player).broadcast()
            return true
        }
        this.connections.remove(player.connection)
        return false
    }

    /**
     * This removes a given player from the minigame.
     * This also removes the player from all the minigame UI.
     *
     * If successful then the [MinigameRemovePlayerEvent] is
     * broadcast.
     *
     * @param player The player to remove.
     */
    public fun removePlayer(player: ServerPlayer) {
        val wasOffline = this.offline.remove(player.gameProfile)
        if (wasOffline || this.connections.remove(player.connection)) {
            if (wasOffline) {
                Arcade.logger.warn("Removed offline player?!")
            }
            MinigameRemovePlayerEvent(this, player).broadcast()
            player.minigame.removeMinigame()
        }
    }

    /**
     * This adds a level handle to the minigame.
     *
     * This will automatically delete the level after the
     * minigame ends.
     *
     * @param handle The RuntimeWorldHandle to delete after the minigame closes.
     */
    public fun addLevelHandle(handle: RuntimeWorldHandle) {
        this.levelHandles.add(handle)
    }

    /**
     * This gets all the tracked players who are
     * playing this minigame.
     *
     * @return All the playing players.
     */
    public fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    /**
     * This gets all profiles of the player's that
     * were playing the minigame but are now offline.
     *
     * @return All the offline player's profiles.
     */
    public fun getOfflinePlayerProfiles(): List<GameProfile> {
        return this.offline.toList()
    }

    /**
     * This gets all the player profiles that are playing this minigame,
     * whether online or offline.
     *
     * @return All the player's profiles.
     */
    public fun getAllPlayerProfiles(): List<GameProfile> {
        return this.getPlayers().map { it.gameProfile }.concat(this.getOfflinePlayerProfiles())
    }

    /**
     * This gets all the teams that are playing in the minigame.
     *
     * @return The collection of player teams.
     */
    public fun getPlayerTeams(): Collection<PlayerTeam> {
        val teams = HashSet<PlayerTeam>()
        for (player in this.getPlayers()) {
            teams.add(this.server.scoreboard.getPlayersTeam(player.gameProfile.name) ?: continue)
        }
        return teams
    }

    /**
     * This gets all the teams that are playing in the minigame,
     * including offline teams.
     *
     * @return The collection of player teams.
     */
    public fun getAllPlayerTeams(): Collection<PlayerTeam> {
        val teams = HashSet<PlayerTeam>()
        for (profile in this.getAllPlayerProfiles()) {
            teams.add(this.server.scoreboard.getPlayersTeam(profile.name) ?: continue)
        }
        return teams
    }

    /**
     * This gets whether a given player is playing in the minigame.
     *
     * @param player The player to check whether they are playing.
     * @return Whether the player is playing.
     */
    public fun hasPlayer(player: ServerPlayer): Boolean {
        return this.connections.contains(player.connection)
    }

    /**
     * This checks whether a given player uuid is playing in the minigame.
     *
     * @param uuid The uuid of the player you want to check.
     * @return Whether the player is playing.
     */
    public fun hasPlayer(uuid: UUID): Boolean {
        val player = PlayerUtils.player(uuid)
        if (player != null) {
            return this.hasPlayer(player)
        }
        return this.offline.any { it.id == uuid }
    }

    /**
     * This checks whether a given level is part of this minigame.
     *
     * @param level The level to check whether is part of the minigame.
     * @return Whether the level is part of the minigame.
     */
    public fun hasLevel(level: ServerLevel): Boolean {
        return this.getLevels().contains(level)
    }

    /**
     * This gets all the registered [GameSetting]s for
     * this minigame.
     *
     * @return A collection of all the settings.
     */
    public fun getGameSettings(): Collection<GameSetting<*>> {
        return this.gameSettings.values.map { it.setting }
    }

    /**
     * This gets a setting for a given name.
     *
     * @param name The name of the given setting.
     * @return The setting, may be null if non-existent.
     */
    public fun getGameSetting(name: String): GameSetting<*>? {
        return this.gameSettings[name]?.setting
    }

    /**
     * This creates a [MenuProvider] which provides a GUI
     * for updating the minigame's [GameSetting]s.
     *
     * @param components The screen components to use for the GUI, by default [DefaultMinigameSettingsComponent].
     * @return The [MenuProvider] for the settings screen.
     */
    public open fun createRulesMenu(components: SelectionScreenComponents = DefaultMinigameSettingsComponent): MenuProvider {
        return ScreenUtils.createMinigameSettingsMenu(this, components)
    }

    /**
     * This gets the [MinigameResources] for this minigame which
     * will be applied when the player joins this minigame.
     */
    public open fun getResources(): MinigameResources {
        return MinigameResources.NONE
    }

    /**
     * This will pause the minigame, stopping the scheduler
     * from executing any more tasks.
     * This will also broadcast the [MinigamePauseEvent].
     *
     * @see paused
     */
    public fun pause() {
        this.paused = true
        MinigamePauseEvent(this).broadcast()
    }

    /**
     * This will unpause the minigame, resuming the scheduler.
     * This will also broadcast the [MinigameUnpauseEvent].
     *
     * @see paused
     */
    public fun unpause() {
        this.paused = false
        MinigameUnpauseEvent(this).broadcast()
    }

    /**
     * This sets the [GameRules] for all the levels in the minigame.
     *
     * @param modifier The modifier to apply to the game rules.
     * @see GameRules
     */
    public fun setGameRules(modifier: GameRules.() -> Unit) {
        for (level in this.getLevels()) {
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
     */
    public fun close() {
        MinigameCloseEvent(this).broadcast()
        for (player in this.getPlayers()) {
            this.removePlayer(player)
        }
        for (handle in this.levelHandles) {
            handle.delete()
        }
        this.events.unregisterHandler()
        this.events.minigame.clear()
        this.events.phased.clear()
        this.scheduler.minigame.cancelAll()
        this.scheduler.phased.cancelAll()

        this.initialized = false
        this.phase = MinigamePhase.none()

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
        json.add("players", this.getPlayers().toJsonStringArray { it.scoreboardName })
        json.add("offline_players", this.offline.toJsonObject { it.name to JsonPrimitive(it.id?.toString()) })
        json.add("levels", this.getLevels().toJsonStringArray { it.dimension().location().toString() })
        json.add("phases", this.phases.toJsonStringArray { it.id })
        json.addProperty("phase", this.phase.id)
        json.addProperty("paused", this.paused)
        json.add("settings", this.getGameSettings().toJsonObject { it.name to it.serializeValue() })
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
     * This should set the phase to the initial phase.
     *
     * This will not run if your minigame restart.
     */
    public abstract fun start()

    /**
     * This method initializes the core functionality of the
     * minigame, such as registering events.
     * This method should be called in your implementation's
     * constructor.
     */
    protected open fun initialize() {
        this.registerEvents()
        this.events.registerHandler()
        MinigameUtils.parseMinigameEvents(this.cast())

        Minigames.register(this)

        this.initialized = true
    }

    /**
     * This gets all the [MinigamePhase]s that this [Minigame]
     * allows.
     *
     * The phases **do not** have to be in order, any duplicates
     * will also be removed.
     * Further you do not need to include the default phases
     * ([MinigamePhase.none] and [MinigamePhase.end]), they'll
     * be included automatically.
     *
     * This method will only be invoked **once**; when the
     * minigame is initialized, the phases are then stored in
     * a collection for the rest of the minigames lifetime.
     *
     * @return A collection of all the valid phases the minigame can be in.
     */
    @OverrideOnly
    protected abstract fun getPhases(): Collection<MinigamePhase<M>>

    /**
     * This gets all the [ServerLevel]s that the [Minigame] is in.
     *
     * This method is used for [hasLevel], to see if the minigame
     * has a given level, and further for debugging purposes.
     *
     * @return A collection of levels that the minigame is in.
     */
    @OverrideOnly
    protected abstract fun getLevels(): Collection<ServerLevel>

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

    /**
     * This registers a [DisplayableGameSetting] to this minigame, and this returns
     * the [GameSetting] which can be delegated in Kotlin.
     *
     * It is recommended that you create an inner class in your [Minigame]
     * implementation where you register and delegate all your settings:
     * ```kotlin
     * class MyMinigame: Minigame(/* ... */) {
     *     val settings = Settings()
     *
     *     // ...
     *
     *     inner class Settings {
     *         val foo = registerSetting(
     *             DisplayableGameSettingBuilder.boolean()
     *                 .name("foo")
     *                 .display(ItemStack(Items.GLOWSTONE).literalNamed("Foo"))
     *                 .defaultOptions()
     *                 .value(true)
     *                 .build()
     *         )
     *
     *         val bar = registerSetting(
     *             DisplayableGameSettingBuilder.double()
     *                 .name("bar")
     *                 .display(ItemStack(Items.DIAMOND).literalNamed("Bar"))
     *                 .option("first", ItemStack(Items.FERN), 1.0)
     *                 .option("second", ItemStack(Items.GRASS), 100.0)
     *                 .value(0.0)
     *                 .build()
     *         )
     *     }
     * }
     * ```
     *
     * All registered settings can be modified using a UI in-game using the command:
     *
     * `/minigame settings <minigame-uuid>`
     *
     * Alternatively you can do it directly with commands:
     *
     * `/minigame settings <minigame-uuid> set <setting> from option <option>`
     *
     * `/minigame settings <minigame-uuid> set <setting> from value <value>`
     *
     * @see DisplayableGameSettingBuilder
     */
    public fun <T: Any> registerSetting(displayed: DisplayableGameSetting<T>): GameSetting<T> {
        val setting = displayed.setting
        this.gameSettings[setting.name] = displayed
        return setting
    }

    private fun getAllPhases(): List<MinigamePhase<M>> {
        val phases = HashSet(this.getPhases())
        phases.add(MinigamePhase.none())
        phases.add(MinigamePhase.end())
        return phases.sortedWith { a, b -> a.compareTo(b) }
    }

    private fun registerEvents() {
        this.events.register<ServerTickEvent> { this.onServerTick() }
        this.events.register<PlayerJoinEvent> { this.onPlayerJoin(it) }
        this.events.register<PlayerDeathEvent> { this.onPlayerDeath(it) }
        this.events.register<PlayerDamageEvent>(Int.MAX_VALUE) { this.onPlayerDamage(it) }
        this.events.register<PlayerLeaveEvent>(Int.MAX_VALUE) { this.onPlayerLeave(it) }
        this.events.register<MinigameAddPlayerEvent> { this.onPlayerAdd(it) }
        this.events.register<MinigameRemovePlayerEvent> { this.onPlayerRemove(it) }
    }

    private fun onServerTick() {
        if (!this.paused) {
            this.ui.tick()
            this.scheduler.tick()
        }
    }

    private fun onPlayerJoin(event: PlayerJoinEvent) {
        this.stats.getOrCreateStat(event.player, ArcadeStats.RELOGS).increment()
    }

    private fun onPlayerDeath(event: PlayerDeathEvent) {
        this.stats.getOrCreateStat(event.player, ArcadeStats.DEATHS).increment()

        val killer = event.source.entity
        if (killer is ServerPlayer && this.hasPlayer(killer)) {
            this.stats.getOrCreateStat(killer, ArcadeStats.KILLS).increment()
        }
    }

    private fun onPlayerDamage(event: PlayerDamageEvent) {
        val (player, _, source) = event
        val amount = event.resultOrElse { event.amount }
        this.stats.getOrCreateStat(player, ArcadeStats.DAMAGE_TAKEN).increment(amount)

        val attacker = source.entity
        if (attacker is ServerPlayer && this.hasPlayer(attacker)) {
            this.stats.getOrCreateStat(attacker, ArcadeStats.DAMAGE_DEALT).increment(amount)
        }
    }

    private fun onPlayerLeave(event: PlayerLeaveEvent) {
        if (this.connections.remove(event.player.connection)) {
            this.offline.add(event.player.gameProfile)
        }
    }

    private fun onPlayerAdd(event: MinigameAddPlayerEvent) {
        val (_, player) = event
        this.getResources().sendTo(player)
        this.server.commands.sendCommands(player)
    }

    private fun onPlayerRemove(event: MinigameRemovePlayerEvent) {
        this.server.commands.sendCommands(event.player)
    }
}