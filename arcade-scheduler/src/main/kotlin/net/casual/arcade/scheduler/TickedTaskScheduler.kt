/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*
import java.util.function.IntFunction
import kotlin.jvm.optionals.getOrNull

@Deprecated("Use TickedTaskScheduler instead")
public typealias TickedScheduler = TickedTaskScheduler

/**
 * This class is an implementation of [MinecraftTaskScheduler] which
 * allows you to schedule [Task]s for a later time on the
 * main server thread.
 *
 * @see MinecraftTaskScheduler
 * @see GlobalTickedScheduler
 */
public class TickedTaskScheduler: MinecraftTaskScheduler {
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
            queue.forEach(Runnable::run)
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
        val queue = this.tasks.remove(this.tickCount + delta)
        for (task in queue) {
            if (task is CancellableTask) {
                task.cancel()
            }
        }
    }

    /**
     * This cancels all the tasks that are currently
     * scheduled in the scheduler.
     */
    public fun cancelAll(): Boolean {
        if (this.tasks.isEmpty()) {
            return false
        }
        for (ticked in this.tasks.values) {
            for (task in ticked) {
                if (task is CancellableTask) {
                    task.cancel()
                }
            }
        }
        this.tasks.clear()
        return true
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [delay].
     *
     * @param delay The duration to wait before running the [task].
     * @param task The task to be scheduled.
     */
    override fun schedule(delay: MinecraftTimeDuration, task: Task) {
        this.tasks.computeIfAbsent(this.tickCount + delay.ticks, IntFunction { ArrayDeque() }).add(task)
    }

    public fun serialize(output: ValueOutput.ValueOutputList, context: TaskSerializationContext) {
        for ((tick, queue) in this.tasks) {
            val delay = tick - this.tickCount
            for (task in queue) {
                val identity = context.storeTask(task)
                val data = output.addChild()
                data.putInt("uid", identity)
                data.putInt("delay", delay)
            }
        }
    }

    public fun deserialize(input: ValueInput.ValueInputList, context: TaskCreationContext) {
        for (data in input) {
            val ticks = data.getInt("delay").getOrNull() ?: continue
            val identity = data.getInt("uid").getOrNull() ?: continue
            val task = context.getTask(identity)
            if (task != null) {
                this.schedule(ticks.Ticks, task)
            }
        }
    }
}