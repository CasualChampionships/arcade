/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.scheduler

/**
 * This interface is for any dispatchers that utilize
 * [MinecraftScheduler]s. This way [net.casual.arcade.utils.coroutine.delay]
 * can correctly delay using the scheduler instead of
 * the (global) main-thread delay loop.
 */
public interface MinecraftSchedulerHolder {
    public val scheduler: MinecraftScheduler
}