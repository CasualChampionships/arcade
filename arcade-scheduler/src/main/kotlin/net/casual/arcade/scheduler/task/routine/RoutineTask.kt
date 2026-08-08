/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.routine

import com.mojang.serialization.Codec
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.error.RichResult
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.startCoroutine
import kotlin.jvm.optionals.getOrNull

/**
 * This task handles executing a [Routine].
 */
internal class RoutineTask<O>(
    private val routine: Routine<O>,
    private val owner: O,
    private val journal: RoutineJournal
): Task, ScheduledTask {
    private var scheduler: TickedScheduler? = null
    private var continuation: Continuation<Unit>? = null

    private var index = 0
    private var replayTo = 0
    private var finished = false
    private var cancelling = false
    private var rehydrating = false
    private var remaining: MinecraftTimeDuration? = null

    override val isFinished: Boolean
        get() = this.finished

    fun attach(scheduler: TickedScheduler) {
        this.scheduler = scheduler
    }

    /**
     * Replays this routine's body up to the point it was serialized at,
     * recreating any state it holds outside of its steps.
     *
     * The routine is left suspended at that point; it is not resumed
     * until its [remaining] duration has elapsed.
     *
     * @param remaining The duration left before this routine resumes.
     */
    fun rehydrate(remaining: MinecraftTimeDuration) {
        if (this.finished || this.cancelling || this.continuation != null || this.journal.cursor < 0) {
            return
        }
        this.replay(remaining)
    }

    override fun run() {
        if (this.finished) {
            return
        }
        val continuation = this.continuation
        if (continuation != null) {
            this.continuation = null
            continuation.resume(Unit)
            return
        }
        this.start(this.journal.cursor + 1)
    }

    override fun cancel() {
        if (this.finished || this.cancelling) {
            return
        }
        if (this.continuation == null) {
            if (this.journal.cursor < 0) {
                this.finished = true
                return
            }

            // If the task wasn't resumed after deserialization
            // we still need to run up to the point of serialization
            // so we can properly cancel the routine at the correct position
            this.replay(null)
        }

        val continuation = this.continuation
        if (continuation == null) {
            this.finished = true
            return
        }
        this.continuation = null
        this.cancelling = true
        continuation.resumeWithException(RoutineCancelledException())
    }

    fun serialize(output: ValueOutput) {
        output.putInt("version", this.routine.version)
        output.store("routine", Routine.CODEC, this.routine)
        this.journal.serialize(output.child("journal"))
    }

    private fun replay(remaining: MinecraftTimeDuration?) {
        this.remaining = remaining
        this.rehydrating = true
        try {
            this.start(this.journal.cursor)
        } finally {
            this.rehydrating = false
            this.remaining = null
        }
    }

    private fun start(replayTo: Int) {
        this.index = 0
        this.replayTo = replayTo

        val routine = this.routine
        val block: suspend RoutineScope<O>.() -> Unit = { with(routine) { run() } }
        block.startCoroutine(Scope(this), Completion(this))
    }

    private fun diverged(message: String): Nothing {
        this.finished = true
        this.cancelling = true
        this.continuation = null
        ArcadeUtils.logger.error("Routine ${this.routine.javaClass.name} diverged on replay, aborting: $message")
        throw RoutineDivergedException()
    }

    private class Scope<O>(private val task: RoutineTask<O>): RoutineScope<O> {
        override val owner: O
            get() = this.task.owner

        override suspend fun delay(
            duration: MinecraftTimeDuration,
            onDelay: (remaining: MinecraftTimeDuration) -> Unit
        ) {
            if (this.task.cancelling) {
                return
            }

            val index = this.task.index++
            if (index < this.task.replayTo) {
                val message = this.task.journal.verify(index, RoutineJournal.Kind.Delay, null)
                if (message != null) {
                    this.task.diverged(message)
                }
                return
            }

            this.task.journal.record(index, RoutineJournal.Kind.Delay, null)
            this.task.journal.suspendedAt(index)

            if (this.task.rehydrating) {
                onDelay.invoke(this.task.remaining ?: duration)

                // We don't actually schedule the task,
                // but we need this to allows us to cancel the coroutine
                return suspendCoroutineUninterceptedOrReturn { continuation ->
                    this.task.continuation = continuation
                    COROUTINE_SUSPENDED
                }
            }

            val scheduler = this.task.scheduler
            if (scheduler == null) {
                this.task.finished = true
                ArcadeUtils.logger.error("Routine ${this.task.routine.javaClass.name} has no scheduler, cannot delay")
                return
            }

            onDelay.invoke(duration)

            return suspendCoroutineUninterceptedOrReturn { continuation ->
                this.task.continuation = continuation
                scheduler.schedule(duration, this.task)
                COROUTINE_SUSPENDED
            }
        }

        override suspend fun step(id: String?, block: () -> Unit) {
            val index = this.task.index++
            if (index < this.task.replayTo) {
                val message = this.task.journal.verify(index, RoutineJournal.Kind.Step, id)
                if (message != null) {
                    this.task.diverged(message)
                }
                return
            }
            this.task.journal.record(index, RoutineJournal.Kind.Step, id)
            block.invoke()
        }

        override suspend fun <T> step(codec: Codec<T>, id: String?, block: () -> T): T {
            val index = this.task.index++
            if (index < this.task.replayTo) {
                val message = this.task.journal.verify(index, RoutineJournal.Kind.Step, id)
                if (message != null) {
                    this.task.diverged(message)
                }
                val decoded = this.task.journal.entry(index)?.value?.decode(codec)
                if (decoded != null) {
                    return decoded
                }
                return block.invoke()
            }

            val value = block.invoke()
            this.task.journal.record(index, RoutineJournal.Kind.Step, id, RoutineJournal.Value.of(codec, value))
            return value
        }
    }

    private class Completion<O>(private val task: RoutineTask<O>): Continuation<Unit> {
        override val context = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {
            this.task.finished = true
            this.task.continuation = null
            val exception = result.exceptionOrNull()
            if (exception != null && exception !is CancellationException) {
                ArcadeUtils.logger.error("Exception while running routine ${this.task.routine.javaClass.name}", exception)
            }
        }
    }

    private class RoutineCancelledException: CancellationException("Routine was cancelled")

    private class RoutineDivergedException: CancellationException("Routine diverged on replay")

    companion object {
        fun create(input: ValueInput, owner: Any?): RichResult<RoutineTask<*>> {
            val routine = input.read("routine", Routine.CODEC).getOrNull()
                ?: return RichResult.failure("Failed to read routine")

            val version = input.getIntOr("version", 1)
            if (version != routine.version) {
                return RichResult.failure(
                    "Routine ${routine.javaClass.simpleName} was saved at version $version " +
                        "but is now version ${routine.version}, it cannot safely be replayed"
                )
            }

            val journal = RoutineJournal.deserialize(input.childOrEmpty("journal"))
            @Suppress("UNCHECKED_CAST")
            return RichResult.success(RoutineTask(routine as Routine<Any?>, owner, journal))
        }
    }
}
