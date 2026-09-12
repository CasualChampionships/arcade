/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.scope

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.EventListenerHandle
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.threading.ThreadingStrategy
import net.casual.arcade.events.threading.ThreadingTarget
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.ListenerFilter
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.managers.MinigameEventHandler
import net.casual.arcade.minigame.phase.MinigamePhaseLifetime
import net.casual.arcade.minigame.utils.MinigameUtils.addEventListener
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.side.LogicalSide
import net.casual.arcade.utils.time.MinecraftTimeDuration
import java.util.function.Consumer
import net.casual.arcade.scheduler.utils.schedule as scheduleRoutine

/**
 * A scope owns tasks and event listeners that are scheduled
 * or registered through it. Whenever the scope is closed then
 * everything is cancelled or unregistered.
 *
 * A scope is closed whenever its specified [lifetime] doesn't
 * survive across a phase transition, when the owning [minigame]
 * is closed, or when [close] is called.
 *
 * The [MinigameScopes.root] and [MinigameScopes.current] scopes are
 * managed by the minigame; they cannot be closed with [close].
 *
 * Scopes are created with [MinigameScopes.create]:
 * ```
 * val minigame: Minigame = // ...
 * val scope = minigame.scopes.create(MinigamePhaseLifetime.Current)
 * // This remains registered until the phase changes
 * scope.register<PlayerDeathEvent> { (player) ->
 *     player.sendSystemMessage(Component.literal("You died!"))
 * }
 * // This will be cancelled if the phase changes before its executed
 * scope.schedule(30.Seconds) {
 *     minigame.chat.broadcast(Component.literal("30 seconds have passed!"))
 * }
 * ```
 *
 * @see MinigameScopes
 * @see MinigamePhaseLifetime
 */
public class MinigameScope internal constructor(
    public val minigame: Minigame,
    public val lifetime: MinigamePhaseLifetime,
    private val scopes: MinigameScopes,
    private val closeable: Boolean
): TickedScheduler, AutoCloseable {
    private val handles = ArrayList<EventListenerHandle>()
    private val tasks = ArrayList<ScheduledTask>()

    private var job: CompletableJob? = null
    private var coroutines: CoroutineScope? = null

    /**
     * Whether this scope has closed.
     */
    public var closed: Boolean = false
        private set

    override val target: LogicalSide
        get() = LogicalSide.Server

    /**
     * @see TickedScheduler.schedule
     */
    override fun schedule(delay: MinecraftTimeDuration, task: Task): ScheduledTask {
        if (this.closed) {
            return this.reject("task")
        }
        return this.track(this.scopes.schedule(delay, task))
    }

    /**
     * @see [TickedScheduler.scheduleRoutine]
     */
    public fun <M: Minigame> schedule(delay: MinecraftTimeDuration, routine: Routine<M>): ScheduledTask {
        if (this.closed) {
            return this.reject(routine.javaClass.simpleName)
        }
        return this.track(this.scopes.schedule(delay, routine))
    }

    override fun asCoroutineScope(): CoroutineScope {
        val existing = this.coroutines
        if (existing != null) {
            return existing
        }
        val parent = this.scopes.coroutineScope().coroutineContext
        val job = SupervisorJob(parent.job)
        if (this.closed) {
            job.cancel()
        }
        val coroutines = CoroutineScope(parent + job)
        this.job = job
        this.coroutines = coroutines
        return coroutines
    }

    /**
     * @see [MinigameEventHandler.register]
     */
    public fun <T: ServerSideEvent> register(
        type: Class<T>,
        filters: Set<ListenerFilter> = ListenerFilter.default(),
        listener: EventListener<T>
    ): EventListenerHandle {
        if (this.closed) {
            return this.reject(type.simpleName, EventListenerHandle.EMPTY)
        }
        val handle = this.minigame.events.register(type, filters, listener)
        this.handles.add(handle)
        return handle
    }

    /**
     * @see [MinigameEventHandler.register]
     */
    public inline fun <reified T: ServerSideEvent> register(
        priority: Int = 1_000,
        phase: Int = BuiltInEventPhases.DEFAULT,
        filters: Set<ListenerFilter> = ListenerFilter.default(),
        strategy: ThreadingStrategy = ThreadingTarget.Default,
        listener: Consumer<T>
    ): EventListenerHandle {
        return this.register(T::class.java, filters, EventListener.of(priority, phase, strategy, listener))
    }

    public fun addEventListener(listener: MinigameEventListener): EventListenerHandle {
        if (this.closed) {
            return this.reject(listener.javaClass.simpleName, EventListenerHandle.EMPTY)
        }
        val handle = this.minigame.addEventListener(listener)
        this.handles.add(handle)
        return handle
    }

    /**
     * This closes the minigame scope and cancels and
     * unregisters all tasks and event listeners.
     *
     * This is idempotent, and won't have any effect
     * when after the first time.
     *
     * The [MinigameScopes.root] and [MinigameScopes.current]
     * scopes cannot be closed this way.
     */
    override fun close() {
        if (!this.closeable) {
            ArcadeUtils.logger.warn("Tried closing minigame managed ${this.lifetime} scope for ${this.minigame.id}")
            return
        }
        this.destroy()
    }

    internal fun expire() {
        if (this.closeable) {
            this.destroy()
        } else {
            this.cancel()
        }
    }

    internal fun destroy() {
        if (this.closed) {
            return
        }
        this.closed = true
        this.cancel()
        this.scopes.remove(this)
    }

    internal fun scheduled(): Collection<ScheduledTask> {
        return this.tasks
    }

    internal fun prune() {
        this.tasks.removeIf(ScheduledTask::isFinished)
    }

    internal fun track(task: ScheduledTask): ScheduledTask {
        this.tasks.add(task)
        return task
    }

    private fun cancel() {
        for (handle in this.handles) {
            handle.remove()
        }
        this.handles.clear()

        for (task in this.tasks) {
            task.cancel()
        }
        this.tasks.clear()

        this.job?.cancel()
        this.job = null
        this.coroutines = null
    }

    private fun reject(what: String): ScheduledTask {
        return this.reject(what, Rejected)
    }

    private fun <T> reject(what: String, value: T): T {
        ArcadeUtils.logger.warn(
            "Rejected '$what' scheduled into a closed ${this.lifetime} scope of minigame ${this.minigame.id}"
        )
        return value
    }

    private object Rejected: ScheduledTask {
        override val isFinished: Boolean
            get() = true

        override fun cancel() {

        }
    }
}
