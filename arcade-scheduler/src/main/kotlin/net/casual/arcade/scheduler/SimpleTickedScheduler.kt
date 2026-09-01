/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineTask
import net.casual.arcade.scheduler.utils.asCoroutineDispatcher
import net.casual.arcade.scheduler.utils.runSafely
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.side.LogicalSide
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*
import java.util.function.IntFunction
import kotlin.jvm.optionals.getOrNull

/**
 * This class is an implementation of [TickedScheduler] which
 * allows you to schedule [Task]s for a later time on the
 * main thread of the given [target] side.
 *
 * You are responsible for ticking this scheduler, and must do
 * so from the same side as [target].
 *
 * @see TickedScheduler
 * @see GlobalTickedScheduler
 */
public class SimpleTickedScheduler(
    override val target: LogicalSide
): TickedScheduler {
    private val tasks: Int2ObjectMap<Queue<ScheduledTaskImpl>> = Int2ObjectOpenHashMap()
    private var tickCount = 0
    private var ticking = false

    private val scope: Lazy<CoroutineScope> = lazy {
        val handler = CoroutineExceptionHandler { _, throwable ->
            ArcadeUtils.logger.error("Uncaught exception while running scheduler coroutine", throwable)
        }
        CoroutineScope(
            this.asCoroutineDispatcher() +
                SupervisorJob() +
                CoroutineName("SimpleTickedScheduler") +
                handler
        )
    }

    /**
     * This advances the scheduler by one tick.
     *
     * All [Task]s that were scheduled for this
     * tick will be run then removed.
     */
    public fun tick() {
        val queue = this.tasks.remove(this.tickCount++)
        if (queue != null) {
            this.ticking = true
            try {
                for (entry in queue) {
                    entry.run()
                }
            } finally {
                this.ticking = false
            }
            queue.clear()
        }
    }

    /**
     * This cancels and removes all tasks with a
     * given tick delta.
     *
     * @param delta The tick delta.
     */
    public fun cancel(delta: Int = 0) {
        val queue = this.tasks.remove(this.bucket(delta)) ?: return
        for (entry in queue) {
            entry.cancel()
        }
    }

    /**
     * This cancels all the tasks that are currently
     * scheduled in the scheduler.
     *
     * All [Task]s, [Routine]s, and coroutines launched via [asCoroutineScope]
     * are all cancelled.
     *
     * The scheduler remains usable afterward; the scope is not permanently
     * cancelled, only its children are.
     *
     * @return Whether any tasks/coroutines were successfully cancelled.
     */
    public fun cancelAll(): Boolean {
        val cancelled = this.cancelCoroutines()
        if (this.tasks.isEmpty()) {
            return cancelled
        }
        val queues = ArrayList(this.tasks.values)
        this.tasks.clear()
        for (queue in queues) {
            for (entry in queue) {
                entry.cancel()
            }
        }
        return true
    }

    override fun asCoroutineScope(): CoroutineScope {
        return this.scope.value
    }

    /**
     * This discards all the tasks that are currently scheduled in the
     * scheduler, *without* cancelling them.
     *
     * Unlike [cancelAll] this runs nothing.
     */
    public fun clear(): Boolean {
        if (this.tasks.isEmpty()) {
            return false
        }
        this.tasks.clear()
        return true
    }

    private fun cancelCoroutines(): Boolean {
        if (!this.scope.isInitialized()) {
            return false
        }
        val job = this.scope.value.coroutineContext.job
        val active = job.children.any()
        job.cancelChildren()
        return active
    }

    override fun schedule(delay: MinecraftTimeDuration, task: Task): ScheduledTask {
        if (task is RoutineTask<*>) {
            task.attach(this)
        }
        val entry = ScheduledTaskImpl(task)
        this.tasks.computeIfAbsent(this.bucket(delay.ticks), IntFunction { ArrayDeque() }).add(entry)
        return entry
    }

    private fun bucket(ticks: Int): Int {
        if (this.ticking) {
            return this.tickCount + (ticks - 1).coerceAtLeast(0)
        }
        return this.tickCount + ticks
    }

    public fun serialize(
        output: ValueOutput.ValueOutputList,
        extra: (ScheduledTask, ValueOutput) -> Unit = { _, _ -> }
    ) {
        for ((tick, queue) in this.tasks) {
            val delay = tick - this.tickCount
            for (entry in queue) {
                val task = entry.task
                if (task !is RoutineTask<*>) {
                    continue
                }
                val data = output.addChild()
                data.putInt("delay", delay)
                task.serialize(data)
                extra.invoke(task, data)
            }
        }
    }

    public fun deserialize(
        input: ValueInput.ValueInputList,
        owner: Any?,
        extra: (ScheduledTask, ValueInput) -> Unit = { _, _ -> }
    ) {
        for (data in input) {
            val ticks = data.getInt("delay").getOrNull() ?: continue
            RoutineTask.create(data, owner).dispatch(
                success = { task ->
                    this.schedule(ticks.Ticks, task)
                    extra.invoke(task, data)
                    task.rehydrate(ticks.Ticks)
                },
                failure = { message -> ArcadeUtils.logger.error("Failed to load routine: $message") }
            )
        }
    }

    private class ScheduledTaskImpl(val task: Task): ScheduledTask {
        private var completed = false

        override val isFinished: Boolean
            get() {
                val task = this.task
                if (task is ScheduledTask) {
                    return task.isFinished
                }
                return this.completed
            }

        override fun cancel() {
            if (this.completed) {
                return
            }
            this.completed = true

            val task = this.task
            if (task is ScheduledTask) {
                task.cancel()
            }
        }

        fun run() {
            if (this.completed) {
                return
            }
            this.completed = true
            this.task.runSafely()
        }
    }

    public companion object {
        public fun server(): SimpleTickedScheduler {
            return SimpleTickedScheduler(LogicalSide.Server)
        }

        public fun client(): SimpleTickedScheduler {
            return SimpleTickedScheduler(LogicalSide.Client)
        }
    }
}