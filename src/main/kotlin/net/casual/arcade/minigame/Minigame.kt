package net.casual.arcade.minigame

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.authlib.GameProfile
import net.casual.arcade.Arcade
import net.casual.arcade.minigame.managers.MinigameAdvancementManager
import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.events.EventHandler
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.TickableUI
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.managers.MinigameCommandManager
import net.casual.arcade.minigame.managers.MinigameEventHandler
import net.casual.arcade.minigame.managers.MinigameScheduler
import net.casual.arcade.minigame.managers.MinigameRecipeManager
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.settings.DisplayableGameSetting
import net.casual.arcade.settings.DisplayableGameSettingBuilder
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.utils.EventUtils.broadcast
import net.casual.arcade.utils.EventUtils.registerHandler
import net.casual.arcade.utils.EventUtils.unregisterHandler
import net.casual.arcade.utils.JsonUtils.toJsonObject
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.MinigameUtils.minigame
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.ScreenUtils.DefaultMinigameScreenComponent
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.MenuProvider
import net.minecraft.world.scores.PlayerTeam
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.util.*
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
 * enum class MyMinigamePhases(
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
 *         this.registerMinigameEvent<MinigameAddPlayerEvent> { (_, player) ->
 *             player.sendSystemMessage(Component.literal("Welcome to My Minigame!"))
 *         }
 *     }
 *
 *     override fun getPhases(): List<MinigamePhase<MyMinigame>> {
 *         return MyMinigamePhases.values().toList()
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
    private val connections: MutableSet<ServerGamePacketListenerImpl>

    private val bossbars: MutableList<CustomBossBar>
    private val nameTags: MutableList<ArcadeNameTag>
    private val tickables: MutableSet<TickableUI>

    private var sidebar: ArcadeSidebar?
    private var display: ArcadeTabDisplay?

    private var initialised: Boolean

    internal val offline: MutableSet<GameProfile>

    internal val gameSettings: MutableMap<String, DisplayableGameSetting<*>>
    internal val phases: List<MinigamePhase<M>>

    internal var uuid: UUID

    public val scheduler: MinigameScheduler
    public val events: MinigameEventHandler
    public val commands: MinigameCommandManager
    public val advancements: MinigameAdvancementManager
    public val recipes: MinigameRecipeManager

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
        this.connections = LinkedHashSet()

        this.bossbars = LinkedList()
        this.nameTags = LinkedList()
        this.tickables = LinkedHashSet()

        this.sidebar = null
        this.display = null

        this.initialised = false

        this.offline = LinkedHashSet()

        this.gameSettings = LinkedHashMap()
        this.phases = this.getAllPhases()

        this.uuid = UUID.randomUUID()

        this.scheduler = MinigameScheduler()
        this.events = MinigameEventHandler(this.cast())
        this.commands = MinigameCommandManager(this.cast())
        this.advancements = MinigameAdvancementManager(this.cast())
        this.recipes = MinigameRecipeManager(this.cast())

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
    public fun isPastPhase(phase: MinigamePhase<M>): Boolean {
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
        this.phase.initialise(self)

        MinigameSetPhaseEvent(self, phase).broadcast()
    }

    /**
     * This adds a player to the minigame.
     * The player may be rejected from joining the minigame.
     * The minigame must be [initialised], and must not yet
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
        if (!this.initialised || this.hasPlayer(player)) {
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

        val event = MinigameAddNewPlayerEvent(this, player).broadcast()
        if (!event.isCancelled()) {
            this.connections.add(player.connection)
            player.minigame.setMinigame(this)
            MinigameAddPlayerEvent(this, player).broadcast()
            return true
        }
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
    public fun getSettings(): Collection<GameSetting<*>> {
        return this.gameSettings.values.map { it.setting }
    }

    /**
     * This gets a setting for a given name.
     *
     * @param name The name of the given setting.
     * @return The setting, may be null if non-existent.
     */
    public fun getSetting(name: String): GameSetting<*>? {
        return this.gameSettings[name]?.setting
    }

    /**
     * This creates a [MenuProvider] which provides a GUI
     * for updating the minigame's [GameSetting]s.
     *
     * @param components The screen components to use for the GUI, by default [DefaultMinigameScreenComponent].
     * @return The [MenuProvider] for the settings screen.
     */
    public open fun createRulesMenu(components: SelectionScreenComponents = DefaultMinigameScreenComponent): MenuProvider {
        return ScreenUtils.createMinigameRulesMenu(this, components)
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
        this.events.unregisterHandler()
        this.events.minigame.clear()
        this.events.phased.clear()
        this.scheduler.minigame.cancelAll()
        this.scheduler.phased.cancelAll()

        this.initialised = false
        this.phase = MinigamePhase.none()

    }

    /**
     * This adds a [CustomBossBar] to the minigame.
     *
     * This will be displayed to all players in the minigame.
     *
     * @param bar The bossbar to add.
     * @see CustomBossBar
     */
    public fun addBossbar(bar: CustomBossBar) {
        this.bossbars.add(bar)
        this.loadUI(bar)
    }

    /**
     * This removes a [CustomBossBar] from the minigame.
     *
     * All players who were shown the bossbar will no longer
     * be displayed the bossbar.
     *
     * @param bar The bar to remove.
     */
    public fun removeBossbar(bar: CustomBossBar) {
        if (this.bossbars.remove(bar)) {
            this.removeUI(bar)
        }
    }

    /**
     * This removes **ALL** bossbars from the minigame.
     */
    public fun removeAllBossbars() {
        this.removeAllUI(this.bossbars)
    }

    /**
     * This adds a [ArcadeNameTag] to the minigame.
     *
     * This name tag will be applied to all players in
     * the minigame.
     *
     * @param tag The name tag to add.
     * @see ArcadeNameTag
     */
    public fun addNameTag(tag: ArcadeNameTag) {
        this.nameTags.add(tag)
        this.loadUI(tag)
    }

    /**
     * This removes a [ArcadeNameTag] from the minigame.
     *
     * All players who had the nametag will no longer be
     * displayed the nametag.
     *
     * @param tag The nametag to remove.
     */
    public fun removeNameTag(tag: ArcadeNameTag) {
        if (this.nameTags.remove(tag)) {
            this.removeUI(tag)
        }
    }

    /**
     * This removes **ALL** nametags from the minigame.
     */
    public fun removeAllNameTags() {
        this.removeAllUI(this.nameTags)
    }

    /**
     * This sets the [ArcadeSidebar] for the minigame.
     *
     * This sidebar will be displayed to all the players
     * in the minigame.
     *
     * @param sidebar The sidebar to set.
     */
    public fun setSidebar(sidebar: ArcadeSidebar) {
        this.removeSidebar()
        this.sidebar = sidebar
        this.loadUI(sidebar)
    }

    /**
     * This removes the minigame sidebar.
     *
     * All players who were displayed the sidebar
     * will no longer be displayed the sidebar.
     */
    public fun removeSidebar() {
        this.removeUI(this.sidebar)
        this.sidebar = null
    }

    /**
     * This sets the [ArcadeTabDisplay] for the minigame.
     *
     * This tab display will be displayed to all the players
     * in the minigame.
     *
     * @param display The tab display to set.
     */
    public fun setTabDisplay(display: ArcadeTabDisplay) {
        this.removeTabDisplay()
        this.display = display
        this.loadUI(display)
    }

    /**
     * This removes the minigame tab display.
     *
     * All players who were displayed the tab display
     * will no longer be displayed the tab display.
     */
    public fun removeTabDisplay() {
        this.removeUI(this.display)
        this.display = null
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
        json.addProperty("initialised", this.initialised)
        json.addProperty("serializable", this is SavableMinigame)
        json.addProperty("uuid", this.uuid.toString())
        json.addProperty("id", this.id.toString())
        json.add("players", this.getPlayers().toJsonStringArray { it.scoreboardName })
        json.add("offline_players", this.offline.toJsonObject { it.name to JsonPrimitive(it.id?.toString()) })
        json.add("levels", this.getLevels().toJsonStringArray { it.dimension().location().toString() })
        json.add("phases", this.phases.toJsonStringArray { it.id })
        json.addProperty("phase", this.phase.id)
        json.addProperty("paused", this.paused)
        json.addProperty("bossbars", this.bossbars.size)
        json.addProperty("nametags", this.nameTags.size)
        json.addProperty("has_sidebar", this.sidebar != null)
        json.addProperty("has_display", this.display != null)
        json.add("settings", this.getSettings().toJsonObject { it.name to it.serializeValue() })
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
        return CustomisableConfig.GSON.toJson(this.getDebugInfo())
    }

    /**
     * Starts the minigame.
     * This should set the phase to the initial phase.
     *
     *  This will not be run if your minigame restart.
     */
    public abstract fun start()

    /**
     * This method initializes the core functionality of the
     * minigame, such as registering events.
     * This method should be called in your implementation's
     * constructor.
     */
    protected open fun initialise() {
        this.registerEvents()
        this.events.registerHandler()

        Minigames.register(this)

        this.initialised = true
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
    protected fun <T: Any> registerSetting(displayed: DisplayableGameSetting<T>): GameSetting<T> {
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
        this.events.register<PlayerLeaveEvent>(Int.MAX_VALUE) { this.onPlayerLeave(it.player) }
        this.events.register<ServerStoppedEvent> { this.close() }
        this.events.register<MinigameAddPlayerEvent> { this.onPlayerAdd(it.player) }
        this.events.register<MinigameRemovePlayerEvent> { this.onPlayerRemove(it.player) }
    }

    private fun onServerTick() {
        if (!this.paused) {
            this.scheduler.tick()
            for (tickable in this.tickables) {
                tickable.tick()
            }
        }
    }

    private fun onPlayerLeave(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            this.offline.add(player.gameProfile)
        }
    }

    private fun onPlayerAdd(player: ServerPlayer) {
        this.bossbars.forEach { it.addPlayer(player) }
        this.nameTags.forEach { it.addPlayer(player) }
        this.sidebar?.addPlayer(player)
        this.display?.addPlayer(player)

        this.getResources().sendTo(player)
        this.server.commands.sendCommands(player)
    }

    private fun onPlayerRemove(player: ServerPlayer) {
        this.nameTags.forEach { it.removePlayer(player) }
        this.bossbars.forEach { it.removePlayer(player) }
        this.sidebar?.removePlayer(player)
        this.display?.removePlayer(player)

        this.server.commands.sendCommands(player)
    }

    private fun loadUI(ui: PlayerUI) {
        for (player in this.getPlayers()) {
            ui.addPlayer(player)
        }
        if (ui is TickableUI) {
            this.tickables.add(ui)
        }
    }

    private fun removeUI(ui: PlayerUI?) {
        if (ui != null) {
            ui.clearPlayers()
            if (ui is TickableUI) {
                this.tickables.remove(ui)
            }
        }
    }

    private fun removeAllUI(uis: MutableCollection<out PlayerUI>) {
        for (ui in uis) {
            this.removeUI(ui)
        }
        uis.clear()
    }
}