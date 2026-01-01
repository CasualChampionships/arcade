/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Delay
import net.casual.arcade.utils.time.MinecraftTimeDuration

/**
 * This interface is for any dispatchers that utilize
 * schedulers. This way [delay] can correctly delay using the
 * scheduler instead of the (global) main-thread delay loop.
 */
public interface MinecraftSchedulerDelay {
    public fun scheduleResumeAfterDelay(delay: MinecraftTimeDuration, continuation: CancellableContinuation<Unit>)
}