/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.routine

import com.mojang.serialization.Codec
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.EventListenerHandle
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.threading.ThreadingTarget
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.error.RichResult
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
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
    private var scheduler: SimpleTickedScheduler? = null
    private var continuation: Continuation<Any?>? = null
    private var listener: EventListenerHandle? = null

    private var index = 0
    private var replayTo = 0
    private var finished = false
    private var cancelling = false
    private var rehydrating = false
    private var running = false
    private var cancelRequested = false
    private var remaining: MinecraftTimeDuration? = null

    override val isFinished: Boolean
        get() = this.finished

    fun attach(scheduler: SimpleTickedScheduler) {
        this.scheduler = scheduler
    }

    /**
     * Replays this routine's body up to the point it was serialized at,
     * recreating any state it holds outside of its steps.
     *
     * The routine is left suspended at that point; it is not resumed
     * until its [remaining] duration has elapsed.
     *
     * @param remaining The duration left before this routine resumes,
     *   `null` if the routine was awaiting an event.
     */
    fun rehydrate(remaining: MinecraftTimeDuration?) {
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
            this.running { continuation.resume(Unit) }
            return
        }
        this.start(this.journal.cursor + 1)
    }

    override fun cancel() {
        if (this.finished || this.cancelling) {
            return
        }
        if (this.running) {
            this.cancelRequested = true
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
        this.running { block.startCoroutine(Scope(this), Completion(this)) }
    }

    private fun <E: Event, T: E> listen(
        type: Class<T>,
        registry: ListenerRegistry<E>,
        priority: Int,
        phase: Int,
        predicate: (T) -> Boolean,
        continuation: Continuation<T>
    ) {
        this.suspendAt(continuation)
        this.scheduler?.startAwaiting(this)

        val listener = EventListener.of<T>(priority, phase, ThreadingTarget.ForceMainThread) { event ->
            this.received(event, predicate)
        }
        this.listener = registry.register(type, listener)
    }

    private fun <T: Event> received(event: T, predicate: (T) -> Boolean) {
        if (this.finished || this.cancelling || !predicate.invoke(event)) {
            return
        }
        val continuation = this.continuation ?: return
        this.continuation = null
        this.running { continuation.resume(event) }
    }

    private fun suspendAt(continuation: Continuation<*>) {
        @Suppress("UNCHECKED_CAST")
        this.continuation = continuation as Continuation<Any?>
    }

    fun stopListening() {
        this.listener?.remove()
        this.listener = null
    }

    private fun stopAwaiting() {
        this.stopListening()
        this.scheduler?.stopAwaiting(this)
    }

    private inline fun running(block: () -> Unit) {
        val previous = this.running
        this.running = true
        try {
            block.invoke()
        } finally {
            this.running = previous
        }
    }

    private fun throwIfCancelled() {
        if (this.cancelRequested && !this.cancelling) {
            this.cancelRequested = false
            this.cancelling = true
            throw RoutineCancelledException()
        }
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
            this.task.throwIfCancelled()

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
                    this.task.suspendAt(continuation)
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
                this.task.suspendAt(continuation)
                scheduler.schedule(duration, this.task)
                COROUTINE_SUSPENDED
            }
        }

        override suspend fun awaitCancellation(): Nothing {
            if (this.task.cancelling) {
                throw RoutineCancelledException()
            }
            this.task.throwIfCancelled()

            val index = this.task.index++
            if (index < this.task.replayTo) {
                val message = this.task.journal.verify(index, RoutineJournal.Kind.AwaitCancellation, null)
                this.task.diverged(message ?: "routine resumed past awaitCancellation at index $index")
            }

            this.task.journal.record(index, RoutineJournal.Kind.AwaitCancellation, null)
            this.task.journal.suspendedAt(index)

            try {
                suspendCoroutineUninterceptedOrReturn<Any?> { continuation ->
                    this.task.suspendAt(continuation)
                    this.task.scheduler?.startAwaiting(this.task)
                    COROUTINE_SUSPENDED
                }
            } finally {
                this.task.stopAwaiting()
            }
            throw RoutineCancelledException()
        }

        override suspend fun <E: Event, T: E> await(
            type: Class<T>,
            registry: ListenerRegistry<E>,
            id: String?,
            priority: Int,
            phase: Int,
            predicate: (T) -> Boolean,
            block: (T) -> Unit
        ) {
            if (!this.task.cancelling) {
                this.await(type, registry, ArcadeExtraCodecs.UNIT, id, priority, phase, predicate, block)
            }
        }

        override suspend fun <E: Event, T: E, R: Any> await(
            type: Class<T>,
            registry: ListenerRegistry<E>,
            codec: Codec<R>,
            id: String?,
            priority: Int,
            phase: Int,
            predicate: (T) -> Boolean,
            block: (T) -> R
        ): R {
            if (this.task.cancelling) {
                throw RoutineCancelledException()
            }
            this.task.throwIfCancelled()

            val index = this.task.index++
            if (index < this.task.replayTo) {
                val message = this.task.journal.verify(index, RoutineJournal.Kind.AwaitEvent, id)
                if (message != null) {
                    this.task.diverged(message)
                }
                return this.task.journal.entry(index)?.value?.decode(codec)
                    ?: this.task.diverged("await at index $index has no recorded value")
            }

            this.task.journal.record(index, RoutineJournal.Kind.AwaitEvent, id)
            this.task.journal.suspendedAt(index)

            val event = try {
                suspendCoroutineUninterceptedOrReturn { continuation ->
                    this.task.listen(type, registry, priority, phase, predicate, continuation)
                    COROUTINE_SUSPENDED
                }
            } finally {
                this.task.stopAwaiting()
            }

            val value = block.invoke(event)
            this.task.journal.record(index, RoutineJournal.Kind.AwaitEvent, id, RoutineJournal.Value.of(codec, value))
            return value
        }

        override suspend fun step(id: String?, block: () -> Unit) {
            this.step(ArcadeExtraCodecs.UNIT, id) { block.invoke() }
        }

        override suspend fun <T> step(codec: Codec<T>, id: String?, block: () -> T): T {
            this.task.throwIfCancelled()

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
