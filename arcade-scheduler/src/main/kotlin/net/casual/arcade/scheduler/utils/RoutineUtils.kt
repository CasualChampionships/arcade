/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineJournal
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.task.routine.RoutineTask
import net.casual.arcade.utils.time.MinecraftTimeDuration

/**
 * Runs another [routine] as part of this one.
 *
 * @param routine The routine to run.
 */
public suspend fun <O> RoutineScope<O>.call(routine: Routine<O>) {
    with(routine) { run() }
}

/**
 * Schedules a [routine] to start after a given [delay].
 *
 * The routine's body does not begin running until the delay elapses,
 * and it may then suspend itself further with [RoutineScope.delay].
 *
 * @param delay The duration to wait before starting the [routine].
 * @param routine The routine to schedule, which must be registered.
 * @param owner The owner the routine runs against.
 * @return A handle which can be used to cancel the routine.
 * @throws IllegalArgumentException If the routine's codec is not registered.
 */
public fun <O> TickedScheduler.schedule(
    delay: MinecraftTimeDuration,
    routine: Routine<O>,
    owner: O
): ScheduledTask {
    require(TaskRegistries.ROUTINE.getKey(routine.codec()) != null) {
        "Routine ${routine.javaClass.name} must be registered in TaskRegistries.ROUTINE before it can be scheduled"
    }
    val task = RoutineTask(routine, owner, RoutineJournal.create())
    this.schedule(delay, task)
    return task
}

/**
 * Schedules a [routine] to start at the end of the current tick.
 *
 * @param routine The routine to schedule, which must be registered.
 * @param owner The owner the routine runs against.
 * @return A handle which can be used to cancel the routine.
 * @throws IllegalArgumentException If the routine's codec is not registered.
 */
public fun <O> TickedScheduler.schedule(routine: Routine<O>, owner: O): ScheduledTask {
    return this.schedule(MinecraftTimeDuration.ZERO, routine, owner)
}
