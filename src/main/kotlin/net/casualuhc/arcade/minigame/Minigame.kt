package net.casualuhc.arcade.minigame

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.core.Event
import net.casualuhc.arcade.events.level.LevelEvent
import net.casualuhc.arcade.events.minigame.*
import net.casualuhc.arcade.events.player.PlayerEvent
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.scheduler.Task
import net.casualuhc.arcade.scheduler.TickedScheduler
import net.casualuhc.arcade.gui.screen.SelectionScreenBuilder
import net.casualuhc.arcade.gui.screen.SelectionScreenComponents
import net.casualuhc.arcade.settings.DisplayableGameSetting
import net.casualuhc.arcade.settings.GameSetting
import net.casualuhc.arcade.utils.MinigameUtils
import net.casualuhc.arcade.utils.MinigameUtils.getMinigame
import net.casualuhc.arcade.utils.MinigameUtils.minigame
import net.casualuhc.arcade.utils.ScreenUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.Level
import java.util.LinkedList
import java.util.UUID
import java.util.function.Consumer

abstract class Minigame(
    val id: ResourceLocation,
    val server: MinecraftServer,
) {
    private val connections = HashSet<ServerGamePacketListenerImpl>()
    private val events = EventHandler()
    private var closed = false

    internal val settings = LinkedHashMap<String, DisplayableGameSetting<*>>()
    internal val phases = HashSet(this.getPhases())
    internal val scheduler = TickedScheduler()
    internal val tasks = ArrayDeque<Task>()

    internal var uuid = UUID.randomUUID()

    var phase = MinigamePhase.NONE
        internal set
    var paused = false
        internal set

    open fun initialise() {
        this.registerEvent<ServerTickEvent> {
            if (!this.paused) {
                this.scheduler.tick()
            }
        }
        this.registerEvent<ServerStoppedEvent> {
            this.close()
        }
        GlobalEventHandler.addHandler(this.events)
        Minigames.register(this)
    }

    fun isPhase(phase: MinigamePhase): Boolean {
        return this.phase === phase
    }

    fun isBeforePhase(phase: MinigamePhase): Boolean {
        return this.phase < phase
    }

    fun isPastPhase(phase: MinigamePhase): Boolean {
        return this.phase > phase
    }

    protected open fun setPhase(phase: MinigamePhase) {
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

    fun addPlayer(player: ServerPlayer) {
        if (!this.closed && !this.hasPlayer(player)) {
            if (player.getMinigame() === this) {
                this.connections.add(player.connection)
                val event = MinigameAddExistingPlayerEvent(this, player)
                GlobalEventHandler.broadcast(event)
                return
            }

            val event = MinigameAddNewPlayerEvent(this, player)
            GlobalEventHandler.broadcast(event)
            if (!event.isCancelled()) {
                this.connections.add(player.connection)
                player.minigame.setMinigame(this)
            }
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            GlobalEventHandler.broadcast(MinigameRemovePlayerEvent(this, player))
            player.minigame.removeMinigame()
        }
    }

    fun getSettings(): Collection<GameSetting<*>> {
        return this.settings.values.map { it.setting }
    }

    fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    fun hasPlayer(player: ServerPlayer): Boolean {
        return this.connections.contains(player.connection)
    }

    fun hasLevel(level: Level): Boolean {
        return this.getLevels().contains(level)
    }

    fun pause() {
        this.paused = true
        GlobalEventHandler.broadcast(MinigamePauseEvent(this))
    }

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

    fun openRulesMenu(player: ServerPlayer, components: SelectionScreenComponents) {
        player.openMenu(ScreenUtils.createMinigameRulesScreen(this, components))
    }

    protected abstract fun getPhases(): Collection<MinigamePhase>

    protected abstract fun getLevels(): Collection<ServerLevel>

    protected fun <T: Any> registerSetting(displayed: DisplayableGameSetting<T>): GameSetting<T> {
        val setting = displayed.setting
        this.settings[setting.name] = displayed
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