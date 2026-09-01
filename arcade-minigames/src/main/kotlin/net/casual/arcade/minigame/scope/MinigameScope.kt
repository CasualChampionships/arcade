/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.scope

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
import net.casual.arcade.minigame.annotation.ListenerFlags.DEFAULT
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.phase.PhaseLifetime
import net.casual.arcade.minigame.utils.MinigameUtils.addEventListener
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.side.LogicalSide
import net.casual.arcade.utils.time.MinecraftTimeDuration
import java.util.function.Consumer

public class MinigameScope internal constructor(
    public val minigame: Minigame,
    public val lifetime: PhaseLifetime,
    private val scopes: MinigameScopes
): TickedScheduler, AutoCloseable {
    private val handles = ArrayList<EventListenerHandle>()
    private val tasks = ArrayList<ScheduledTask>()

    private val job by lazy {
        SupervisorJob(this.scopes.coroutineScope().coroutineContext.job)
    }
    private val coroutineScope by lazy {
        CoroutineScope(this.scopes.coroutineScope().coroutineContext + this.job)
    }

    public var closed: Boolean = false
        private set

    override val target: LogicalSide
        get() = LogicalSide.Server

    override fun schedule(delay: MinecraftTimeDuration, task: Task): ScheduledTask {
        if (this.closed) {
            return this.reject("task")
        }
        return this.track(this.scopes.schedule(delay, task))
    }

    public fun <M: Minigame> schedule(delay: MinecraftTimeDuration, routine: Routine<M>): ScheduledTask {
        if (this.closed) {
            return this.reject(routine.javaClass.simpleName)
        }
        return this.track(this.scopes.schedule(delay, routine))
    }

    override fun asCoroutineScope(): CoroutineScope {
        return this.coroutineScope
    }

    public fun <T: ServerSideEvent> register(
        type: Class<T>,
        flags: Int = DEFAULT,
        listener: EventListener<T>
    ): EventListenerHandle {
        if (this.closed) {
            return this.reject(type.simpleName, EventListenerHandle.EMPTY)
        }
        val handle = this.minigame.events.register(type, flags, listener)
        this.handles.add(handle)
        return handle
    }

    public inline fun <reified T: ServerSideEvent> register(
        priority: Int = 1_000,
        phase: Int = BuiltInEventPhases.DEFAULT,
        flags: Int = DEFAULT,
        strategy: ThreadingStrategy = ThreadingTarget.Default,
        listener: Consumer<T>
    ): EventListenerHandle {
        return this.register(T::class.java, flags, EventListener.of(priority, phase, strategy, listener))
    }

    public fun addEventListener(listener: MinigameEventListener): EventListenerHandle {
        if (this.closed) {
            return this.reject(listener.javaClass.simpleName, EventListenerHandle.EMPTY)
        }
        val handle = this.minigame.addEventListener(listener)
        this.handles.add(handle)
        return handle
    }

    override fun close() {
        if (this.closed) {
            return
        }
        this.closed = true

        for (handle in this.handles) {
            handle.remove()
        }
        this.handles.clear()

        for (task in this.tasks) {
            task.cancel()
        }
        this.tasks.clear()

        this.scopes.remove(this)
        this.job.cancel()
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
