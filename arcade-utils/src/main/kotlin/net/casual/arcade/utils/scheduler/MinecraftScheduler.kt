/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.scheduler

import net.casual.arcade.utils.time.MinecraftTimeDuration

public interface MinecraftScheduler {
    /**
     * This method will schedule a [task] to be run
     * after a given [delay].
     *
     * @param delay The duration to wait before running the [task].
     * @param task The task to be scheduled.
     */
    public fun schedule(delay: MinecraftTimeDuration, task: Runnable)
}