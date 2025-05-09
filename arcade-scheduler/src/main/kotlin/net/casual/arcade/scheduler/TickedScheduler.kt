/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration
import java.util.*
import java.util.function.IntFunction

/**
 * This class is an implementation of [MinecraftScheduler] which
 * allows you to schedule [Task]s for a later time on the
 * main server thread.
 *
 * @see MinecraftScheduler
 * @see GlobalTickedScheduler
 */
public class TickedScheduler: MinecraftScheduler {
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
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [task].
     * @param task The task to be scheduled.
     */
    override fun schedule(duration: MinecraftTimeDuration, task: Task) {
        this.tasks.computeIfAbsent(this.tickCount + duration.ticks, IntFunction { ArrayDeque() }).add(task)
    }

    public fun serialize(context: TaskSerializationContext): JsonArray {
        val tasks = JsonArray()
        for ((tick, queue) in this.tasks) {
            val delay = tick - this.tickCount
            for (task in queue) {
                val identity = context.serializeTask(task) ?: continue
                val data = JsonObject()
                data.addProperty("uid", identity)
                data.addProperty("delay", delay)
                tasks.add(data)
            }
        }
        return tasks
    }

    public fun deserialize(tasks: JsonArray, context: TaskCreationContext) {
        for (data in tasks.objects()) {
            val ticks = data.int("delay")
            val identity = data.int("uid")
            val task = context.createTask(identity)
            if (task != null) {
                this.schedule(ticks.Ticks, task)
            }
        }
    }
}