/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import kotlinx.coroutines.*
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.MinecraftSchedulerDelay
import net.casual.arcade.utils.time.MinecraftTimeDuration
import org.jetbrains.annotations.ApiStatus.Internal
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext

@Internal
public fun interface CoroutineTask: Task

public fun TickedScheduler.asCoroutineDispatcher(): CoroutineDispatcher {
    return MinecraftSchedulerDispatcher(this)
}

@OptIn(InternalCoroutinesApi::class)
private class MinecraftSchedulerDispatcher(
    val scheduler: TickedScheduler
): CoroutineDispatcher(), Delay, MinecraftSchedulerDelay {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!this.scheduler.target.isOnThread()) {
            this.scheduler.schedule(MinecraftTimeDuration.ZERO, CoroutineTask { block.run() })
        } else {
            block.run()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        // We round up the number of ticks
        val ticks = (timeMillis + MS_PER_TICK - 1) / MS_PER_TICK

        val delay = ticks.toInt().Ticks
        this.scheduleResumeAfterDelay(delay, continuation)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun scheduleResumeAfterDelay(delay: MinecraftTimeDuration, continuation: CancellableContinuation<Unit>) {
        val task = CancellableTask.of(CoroutineTask {
            with(continuation) { resumeUndispatched(Unit) }
        })

        this.scheduler.schedule(delay, task)

        continuation.invokeOnCancellation { task.cancel() }
        task.ifCancelled { continuation.cancel() }
    }

    companion object {
        private const val MS_PER_TICK = 50L
    }
}