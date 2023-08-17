package net.casualuhc.arcade.minigame

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.core.Event
import net.casualuhc.arcade.events.minigame.*
import net.casualuhc.arcade.events.player.PlayerEvent
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.scheduler.Task
import net.casualuhc.arcade.scheduler.TickedScheduler
import net.casualuhc.arcade.utils.MinigameUtils.minigame
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.function.Consumer

abstract class Minigame(
    val id: ResourceLocation
) {
    private val connections = HashSet<ServerGamePacketListenerImpl>()
    private val events = EventHandler()
    private var closed = false

    internal val phases = HashSet(this.getPhases())
    internal val scheduler = TickedScheduler()

    var phase = MinigamePhase.NONE
        internal set
    var paused = false
        internal set

    init {
        this.registerEvent<ServerTickEvent> {
            if (!this.paused) {
                this.scheduler.tick()
            }
        }
        this.registerEvent<ServerStoppedEvent> {
            this.close()
        }
        GlobalEventHandler.addHandler(this.events)
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

    fun setPhase(phase: MinigamePhase) {
        if (!this.phases.contains(phase)) {
            throw IllegalArgumentException("Cannot set minigame '${this.id}' phase to ${phase.id}")
        }
        this.scheduler.tasks.clear()
        this.phase = phase

        GlobalEventHandler.broadcast(MinigameSetPhaseEvent(this, phase))
    }

    fun addPlayer(player: ServerPlayer) {
        if (!this.closed && !this.hasPlayer(player)) {
            val event = MinigameAddPlayerEvent(this, player)
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

    fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    fun hasPlayer(player: ServerPlayer): Boolean {
        return this.connections.contains(player.connection)
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

    protected abstract fun getPhases(): Collection<MinigamePhase>

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
        when {
            registerPredicatedEvent(PlayerEvent::class.java, { this.hasPlayer(it.player) }, type, priority, listener) -> { }
            registerPredicatedEvent(MinigameEvent::class.java, { this === it }, type, priority, listener) -> { }
            else -> this.registerEvent(type, priority, listener)
        }
    }

    private fun <T: Event, S: Event> registerPredicatedEvent(
        required: Class<T>,
        predicate: (T) -> Boolean,
        type: Class<S>,
        priority: Int,
        listener: Consumer<S>
    ): Boolean {
        if (required.isAssignableFrom(type)) {
            this.registerEvent(type, priority) {
                @Suppress("UNCHECKED_CAST")
                if (predicate(it as T)) {
                    listener.accept(it)
                }
            }
            return true
        }
        return false
    }
}