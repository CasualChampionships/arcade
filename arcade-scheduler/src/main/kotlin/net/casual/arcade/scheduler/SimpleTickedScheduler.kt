/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.Cancellable
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineTask
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
    private val tasks: Int2ObjectMap<Queue<Task>> = Int2ObjectOpenHashMap()
    private var tickCount = 0

    /**
     * This advances the scheduler by one tick.
     *
     * All [Task]s that were scheduled for this
     * tick will be run then removed.
     */
    public fun tick() {
        val queue = this.tasks.remove(this.tickCount++)
        if (queue != null) {
            for (task in queue) {
                task.runSafely()
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
        val queue = this.tasks.remove(this.tickCount + delta) ?: return
        for (task in queue) {
            this.cancel(task)
        }
    }

    /**
     * This cancels all the tasks that are currently
     * scheduled in the scheduler.
     *
     * Cancelling a [Routine] unwinds it, running any cleanup it has in
     * `finally` blocks. If you instead want to discard the scheduler's
     * state without running anything, use [clear].
     */
    public fun cancelAll(): Boolean {
        if (this.tasks.isEmpty()) {
            return false
        }
        val queues = ArrayList(this.tasks.values)
        this.tasks.clear()
        for (queue in queues) {
            for (task in queue) {
                this.cancel(task)
            }
        }
        return true
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

    private fun cancel(task: Task) {
        if (task is Cancellable) {
            task.cancel()
        }
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [delay].
     *
     * @param delay The duration to wait before running the [task].
     * @param task The task to be scheduled.
     */
    override fun schedule(delay: MinecraftTimeDuration, task: Task) {
        if (task is RoutineTask<*>) {
            task.attach(this)
        }
        this.tasks.computeIfAbsent(this.tickCount + delay.ticks, IntFunction { ArrayDeque() }).add(task)
    }

    public fun serialize(output: ValueOutput.ValueOutputList) {
        for ((tick, queue) in this.tasks) {
            val delay = tick - this.tickCount
            for (task in queue) {
                if (task !is RoutineTask<*>) {
                    continue
                }
                val data = output.addChild()
                data.putInt("delay", delay)
                task.serialize(data)
            }
        }
    }

    public fun deserialize(input: ValueInput.ValueInputList, owner: Any?) {
        for (data in input) {
            val ticks = data.getInt("delay").getOrNull() ?: continue
            RoutineTask.create(data, owner).dispatch(
                success = { task -> this.schedule(ticks.Ticks, task) },
                failure = { message -> ArcadeUtils.logger.error("Failed to load routine: $message") }
            )
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