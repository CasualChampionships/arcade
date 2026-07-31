/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task

/**
 * Something scheduled which can be stopped before it runs.
 *
 * This is implemented by [net.casual.arcade.scheduler.task.impl.CancellableTask]
 * for transient tasks, and by [net.casual.arcade.scheduler.task.routine.RoutineTask] for routines.
 */
public interface Cancellable {
    /**
     * Whether this has already run, or been cancelled.
     */
    public val isFinished: Boolean

    /**
     * Cancels this, preventing it from running.
     */
    public fun cancel()
}
