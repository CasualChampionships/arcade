/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import kotlinx.coroutines.*
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.coroutine.MinecraftSchedulerDelay
import net.casual.arcade.utils.time.MinecraftTimeDuration
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext

public fun TickedScheduler.asCoroutineDispatcher(): CoroutineDispatcher {
    return MinecraftSchedulerDispatcher(this)
}

@OptIn(InternalCoroutinesApi::class)
private class MinecraftSchedulerDispatcher(
    val scheduler: TickedScheduler
): CoroutineDispatcher(), Delay, MinecraftSchedulerDelay {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!this.scheduler.target.isOnThread()) {
            this.scheduler.schedule(MinecraftTimeDuration.ZERO, block::run)
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

    override fun scheduleResumeAfterDelay(delay: MinecraftTimeDuration, continuation: CancellableContinuation<Unit>) {
        val handle = this.scheduler.schedule(delay, ResumeTask(continuation))
        continuation.invokeOnCancellation { handle.cancel() }
    }

    private inner class ResumeTask(
        private val continuation: CancellableContinuation<Unit>
    ): Task, ScheduledTask {
        override val isFinished: Boolean
            get() = !this.continuation.isActive

        override fun cancel() {
            this.continuation.cancel()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun run() {
            with(this.continuation) { resumeUndispatched(Unit) }
        }
    }

    companion object {
        private const val MS_PER_TICK = 50L
    }
}