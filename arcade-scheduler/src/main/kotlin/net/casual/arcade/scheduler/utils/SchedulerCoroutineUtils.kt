/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import net.casual.arcade.scheduler.MinecraftTaskScheduler
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.scheduler.MinecraftSchedulerHolder
import net.casual.arcade.utils.time.MinecraftTimeDuration
import kotlin.coroutines.CoroutineContext

public fun MinecraftTaskScheduler.asCoroutineDispatcher(): CoroutineDispatcher {
    return MinecraftSchedulerDispatcher(this)
}

@OptIn(InternalCoroutinesApi::class)
private class MinecraftSchedulerDispatcher(
    override val scheduler: MinecraftTaskScheduler
): CoroutineDispatcher(), Delay, MinecraftSchedulerHolder {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        scheduler.schedule(MinecraftTimeDuration.ZERO) { block.run() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val ticks = (timeMillis + MS_PER_TICK - 1) / MS_PER_TICK

        val duration = ticks.toInt().Ticks
        val task = CancellableTask.of {
            with(continuation) { resumeUndispatched(Unit) }
        }

        this.scheduler.schedule(duration, task)

        continuation.invokeOnCancellation { task.cancel() }
        task.ifCancelled { continuation.cancel() }
    }

    companion object {
        private const val MS_PER_TICK = 50L
    }
}