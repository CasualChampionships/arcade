package net.casual.arcade.minigame

import net.casual.arcade.events.EventHandler
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.level.LevelEvent
import net.casual.arcade.events.minigame.*
import net.casual.arcade.events.player.PlayerEvent
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.scheduler.Task
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.settings.DisplayableGameSetting
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.MinigameUtils.minigame
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.ScreenUtils.DefaultMinigameScreenComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.MenuProvider
import net.minecraft.world.level.Level
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayDeque
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
 * As well as the minigames own state, see: [phase], [paused].
 *
 * You can implement your own minigame by extending this class:
 * ```kotlin
 * enum class MyMinigamePhases(
 *     override val id: String
 * ): MinigamePhase {
 *     Grace("grace"),
 *     Active("active"),
 *     DeathMatch("death_match")
 * }
 *
 * class MyMinigame(
 *     server: MinecraftServer
 * ): Minigame(ResourceLocation("modid", "my_minigame"), server) {
 *     val settings = Settings()
 *
 *     init {
 *         this.initialise()
 *     }
 *
 *     override fun getPhases(): Collection<MinigamePhase> {
 *         return MyMinigamePhases.values().toList()
 *     }
 *
 *     override fun getLevels(): Collection<ServerLevel> {
 *         return listOf(LevelUtils.overworld())
 *     }
 *
 *     private fun registerEvents() {
 *         this.registerMinigameEvent<MinigameAddPlayerEvent> { (_, player) ->
 *             player.sendSystemMessage(Component.literal("Welcome to My Minigame!"))
 *         }
 *     }
 *
 *     inner class Settings {
 *         val mySetting by registerSetting(
 *             DisplayableGameSettingBuilder.boolean()
 *                 .name("my_setting")
 *                 .display(ItemStack(Items.GLOWSTONE))
 *                 .defaultOptions()
 *                 .value(true)
 *                 .build()
 *         )
 *     }
 * }
 * ```
 *
 * @param id The [ResourceLocation] of the [Minigame].
 * @param server The [MinecraftServer] that created the [Minigame].
 *
 * @see SavableMinigame
 */
abstract class Minigame(
    /**
     * The [ResourceLocation] of the [Minigame].
     */
    val id: ResourceLocation,
    /**
     * The [MinecraftServer] that created the [Minigame].
     */
    val server: MinecraftServer,
) {
    private val connections = HashSet<ServerGamePacketListenerImpl>()
    private val events = EventHandler()

    private val bossbars = ArrayList<CustomBossBar>()
    private val nameTags = ArrayList<ArcadeNameTag>()

    private var sidebar: ArcadeSidebar? = null
    private var display: ArcadeTabDisplay? = null
    private var closed = false

    internal val gameSettings = LinkedHashMap<String, DisplayableGameSetting<*>>()
    internal val phases = HashSet(this.getPhases())
    internal val scheduler = TickedScheduler()
    internal val tasks = ArrayDeque<Task>()

    internal var uuid = UUID.randomUUID()

    /**
     * What phase the minigame is currently in.
     * This, for example, may differ from whether there is a
     * grace period, death match, etc.
     *
     * @see setPhase
     * @see isPhase
     */
    var phase = MinigamePhase.NONE
        internal set

    /**
     * When minigames are paused, none of the scheduled
     * tasks will execute until the minigame in unpaused.
     *
     * @see pause
     * @see unpause
     */
    var paused = false
        internal set

    /**
     * This method initializes the core functionality of the
     * minigame, such as registering events.
     * This method should be called in your implementation's
     * constructor.
     */
    protected open fun initialise() {
        this.registerEvent<ServerTickEvent> {
            if (!this.paused) {
                this.scheduler.tick()
            }
        }
        this.registerEvent<ServerStoppedEvent> {
            this.close()
        }
        this.registerMinigameEvent<MinigameAddPlayerEvent> { (_, player) ->
            this.bossbars.forEach { it.addPlayer(player) }
            this.sidebar?.addPlayer(player)
            this.display?.addPlayer(player)
        }
        GlobalEventHandler.addHandler(this.events)
        Minigames.register(this)
    }

    /**
     * Checks whether the minigame is in a given phase.
     *
     * @param phase The phase to check whether the minigame is in.
     * @return Whether the minigame is in that phase.
     */
    fun isPhase(phase: MinigamePhase): Boolean {
        return this.phase === phase
    }

    /**
     * Checks whether the minigame is before a given phase.
     *
     * @param phase The phase to check whether the minigame is before.
     * @return Whether the minigame is before that phase.
     */
    fun isBeforePhase(phase: MinigamePhase): Boolean {
        return this.phase < phase
    }

    /**
     * Checks whether the minigame is past a given phase.
     *
     * @param phase The phase to check whether the minigame has past.
     * @return Whether the minigame is past that phase.
     */
    fun isPastPhase(phase: MinigamePhase): Boolean {
        return this.phase > phase
    }

    /**
     * This sets the phase of the minigame.
     * It will only be set if the given phase is
     * **different** to the current phase and in
     * the [phases] set.
     *
     * When a phase is set, all previous scheduled
     * tasks will be cleared, and post-phase tasks
     * will be run.
     *
     * After this the [MinigameSetPhaseEvent] is
     * broadcasted for listeners.
     *
     * @param phase The phase to set the minigame to.
     * @throws IllegalArgumentException If the [phase] is not in the [phases] set.
     */
    protected open fun setPhase(phase: MinigamePhase) {
        if (this.isPhase(phase)) {
            return
        }
        if (!this.phases.contains(phase)) {
            throw IllegalArgumentException("Cannot set minigame '${this.id}' phase to ${phase.id}")
        }
        this.scheduler.tasks.clear()
        this.phase = phase

        for (task in this.tasks) {
            task.run()
        }
        this.tasks.clear()

        GlobalEventHandler.broadcast(MinigameSetPhaseEvent(this, phase))
    }

    /**
     * This adds a player to the minigame.
     * The player may be rejected from joining the minigame.
     * The minigame must not be [closed], and must not yet
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
    fun addPlayer(player: ServerPlayer): Boolean {
        if (!this.closed && !this.hasPlayer(player)) {
            if (player.getMinigame() === this) {
                this.connections.add(player.connection)
                GlobalEventHandler.broadcast(MinigameAddExistingPlayerEvent(this, player))
                GlobalEventHandler.broadcast(MinigameAddPlayerEvent(this, player))
                return true
            }

            val event = MinigameAddNewPlayerEvent(this, player)
            GlobalEventHandler.broadcast(event)
            if (!event.isCancelled()) {
                this.connections.add(player.connection)
                player.minigame.setMinigame(this)
                GlobalEventHandler.broadcast(MinigameAddPlayerEvent(this, player))
                return true
            }
        }
        return false
    }

    /**
     * This removes a given player from the minigame.
     * If successful then the [MinigameRemovePlayerEvent] is
     * broadcast.
     *
     * @param player The player to remove.
     */
    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            GlobalEventHandler.broadcast(MinigameRemovePlayerEvent(this, player))
            player.minigame.removeMinigame()
        }
    }

    /**
     * This gets all the tracked players who are
     * playing this minigame.
     *
     * @return All the playing players.
     */
    fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    /**
     * This gets whether a given player is playing in the minigame.
     *
     * @param player The player to check whether they are playing.
     * @return Whether the player is playing.
     */
    fun hasPlayer(player: ServerPlayer): Boolean {
        return this.connections.contains(player.connection)
    }

    /**
     * This checks whether a given level is part of this minigame.
     *
     * @param level The level to check whether is part of the minigame.
     * @return Whether the level is part of the minigame.
     */
    fun hasLevel(level: ServerLevel): Boolean {
        return this.getLevels().contains(level)
    }

    /**
     * This gets all the registered [GameSetting]s for
     * this minigame.
     *
     * @return A collection of all the settings.
     */
    fun getSettings(): Collection<GameSetting<*>> {
        return this.gameSettings.values.map { it.setting }
    }

    /**
     * This creates a [MenuProvider] which provides a GUI
     * for updating the minigame's [GameSetting]s.
     *
     * @param components The screen components to use for the GUI, by default [DefaultMinigameScreenComponent].
     * @return The [MenuProvider] for the settings screen.
     */
    open fun createRulesMenu(components: SelectionScreenComponents = DefaultMinigameScreenComponent): MenuProvider {
        return ScreenUtils.createMinigameRulesScreen(this, components)
    }

    /**
     * This will pause the minigame, stopping the scheduler
     * from executing any more tasks.
     * This will also broadcast the [MinigamePauseEvent].
     *
     * @see paused
     */
    fun pause() {
        this.paused = true
        GlobalEventHandler.broadcast(MinigamePauseEvent(this))
    }

    /**
     * This will unpause the minigame, resuming the scheduler.
     * This will also broadcast the [MinigameUnpauseEvent].
     *
     * @see paused
     */
    fun unpause() {
        this.paused = false
        GlobalEventHandler.broadcast(MinigameUnpauseEvent(this))
    }

    fun close() {
        for (player in this.getPlayers()) {
            this.removePlayer(player)
        }
        GlobalEventHandler.broadcast(MinigameCloseEvent(this))
        GlobalEventHandler.removeHandler(this.events)
        this.scheduler.tasks.clear()
        this.closed = true
    }

    fun addBossbar(bar: CustomBossBar) {
        this.bossbars.add(bar)
        this.reloadBossbars()
    }

    fun removeBossbar(bar: CustomBossBar) {
        this.bossbars.remove(bar)
        bar.clearPlayers()
    }

    fun removeBossbars() {
        for (bossbar in this.bossbars) {
            bossbar.clearPlayers()
        }
        this.bossbars.clear()
    }

    fun reloadBossbars() {
        for (bossbar in this.bossbars) {
            for (player in this.getPlayers()) {
                bossbar.addPlayer(player)
            }
        }
    }

    fun addNameTag(tag: ArcadeNameTag) {
        this.nameTags.add(tag)
        this.reloadNameTags()
    }

    fun removeNameTag(tag: ArcadeNameTag) {
        this.nameTags.remove(tag)
        tag.clearPlayers()
    }

    fun removeNameTags() {
        for (tag in this.nameTags) {
            tag.clearPlayers()
        }
        this.nameTags.clear()
    }

    fun reloadNameTags() {
        for (tag in this.nameTags) {
            for (player in this.getPlayers()) {
                tag.addPlayer(player)
            }
        }
    }

    fun setSidebar(sidebar: ArcadeSidebar) {
        this.removeSidebar()

        this.sidebar = sidebar
        for (player in this.getPlayers()) {
            sidebar.addPlayer(player)
        }
    }

    fun removeSidebar() {
        this.sidebar?.clearPlayers()
        this.sidebar = null
    }

    fun setTabDisplay(display: ArcadeTabDisplay) {
        this.removeTabDisplay()

        this.display = display
        for (player in this.getPlayers()) {
            display.addPlayer(player)
        }
    }

    fun removeTabDisplay() {
        this.display?.clearPlayers()
        this.display = null
    }

    override fun toString(): String {
        return """
        Minigame: ${this::class.java.simpleName}
        UUID: ${this.uuid}
        ID: ${this.id}
        Players: ${this.getPlayers().joinToString { it.scoreboardName }}
        Levels: ${this.getLevels().joinToString { it.dimension().location().toString() }}
        Phase: ${this.phase.id}
        Paused: ${this.paused}
        Closed: ${this.closed}
        """.trimIndent()
    }

    protected abstract fun getPhases(): Collection<MinigamePhase>

    protected abstract fun getLevels(): Collection<ServerLevel>

    protected fun <T: Any> registerSetting(displayed: DisplayableGameSetting<T>): GameSetting<T> {
        val setting = displayed.setting
        this.gameSettings[setting.name] = displayed
        return setting
    }

    protected fun schedulePhaseEndTask(task: Task) {
        this.tasks.add(task)
    }

    protected fun schedulePhaseEndTask(runnable: Runnable) {
        this.tasks.add(Task.of(runnable))
    }

    protected fun schedulePhaseTask(time: Int, unit: MinecraftTimeUnit, task: Task) {
        this.scheduler.schedule(time, unit, task)
    }

    protected fun schedulePhaseTask(time: Int, unit: MinecraftTimeUnit, runnable: Runnable) {
        this.scheduler.schedule(time, unit, runnable)
    }

    protected fun scheduleInLoopPhaseTask(delay: Int, interval: Int, duration: Int, unit: MinecraftTimeUnit, runnable: Runnable) {
        this.scheduler.scheduleInLoop(delay, interval, duration, unit, runnable)
    }

    protected inline fun <reified T: Event> registerEvent(priority: Int = 1_000, listener: Consumer<T>) {
        this.registerEvent(T::class.java, priority, listener)
    }

    protected fun <T: Event> registerEvent(type: Class<T>, priority: Int = 1_000, listener: Consumer<T>) {
        this.events.register(type, priority, listener)
    }

    protected inline fun <reified T: Event> registerMinigameEvent(priority: Int = 1_000, listener: Consumer<T>) {
        this.registerMinigameEvent(T::class.java, priority, listener)
    }

    protected fun <T: Event> registerMinigameEvent(type: Class<T>, priority: Int = 1_000, listener: Consumer<T>) {
        val predicates = LinkedList<(T) -> Boolean>()
        if (PlayerEvent::class.java.isAssignableFrom(type)) {
            predicates.add { this.hasPlayer((it as PlayerEvent).player) }
        }
        if (LevelEvent::class.java.isAssignableFrom(type)) {
            predicates.add { this.hasLevel((it as LevelEvent).level) }
        }
        if (MinigameEvent::class.java.isAssignableFrom(type)) {
            predicates.add { (it as MinigameEvent).minigame === this }
        }
        if (predicates.isEmpty()) {
            this.registerEvent(type, priority, listener)
        } else {
            this.registerEvent(type, priority) { event ->
                if (predicates.all { it(event) }) {
                    listener.accept(event)
                }
            }
        }
    }
}