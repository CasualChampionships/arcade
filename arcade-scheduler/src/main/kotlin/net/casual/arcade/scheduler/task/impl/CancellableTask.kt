/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.impl

import com.mojang.serialization.Codec
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.impl.CancellableTask.Companion.of
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.scheduler.task.serialization.TaskFactory
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.error.RichResult
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.Serializable
import kotlin.jvm.optionals.getOrNull

/**
 * This extension of the [Task] interface allows
 * for cancelling of a task.
 *
 * If a task is cancelled, it will no longer run
 * or serialized.
 *
 * @see Task
 */
public sealed class CancellableTask(
    protected val wrapped: Task
): Task {
    protected val cancelled: MutableList<Task> = ArrayList()

    /**
     * Whether the task is cancelled or not.
     */
    public var isCancelled: Boolean = false
        private set

    /**
     * This cancels the task and prevents it from running.
     */
    public fun cancel() {
        if (this.isCancelled) {
            return
        }
        this.isCancelled = true
        for (cancel in this.cancelled) {
            cancel.run()
        }
    }

    /**
     * This adds a callback which will be called
     * when the task is cancelled.
     *
     * @param task The task to add.
     * @return The cancellable task.
     */
    public fun ifCancelled(task: Task): CancellableTask {
        this.cancelled.add(task)
        return this
    }

    /**
     * This makes the Cancellable's task run when
     * if the task is cancelled.
     *
     * @return The cancellable task.
     */
    public fun runIfCancelled(): CancellableTask {
        return this.ifCancelled(this.wrapped)
    }

    /**
     * This will be called when running the task,
     * however, it will check whether the event has
     * been cancelled before running the wrapped
     * task, if the current task is cancelled then
     * it will not run the wrapped task.
     */
    override fun run() {
        if (!this.isCancelled) {
            this.wrapped.run()
        }
    }

    private class Default(wrapped: Task): CancellableTask(wrapped)

    @Internal
    public class Savable(wrapped: Task): CancellableTask(wrapped), SavableTask {
        override val id: Identifier = Companion.id

        override fun serialize(output: ValueOutput, context: TaskSerializationContext) {
            val wrappedRef = context.storeTask(this.wrapped)
            output.putInt("wrapped", wrappedRef)
            val onCancel = output.list("on_cancel", Codec.INT)
            for (cancel in this.cancelled) {
                onCancel.add(context.storeTask(cancel))
            }
            output.putBoolean("is_cancelled", this.isCancelled)
        }

        @Internal
        public companion object: TaskFactory {
            override val id: Identifier = IdentifierUtils.arcade("internal_savable_cancellable")

            override fun create(input: ValueInput, context: TaskCreationContext): RichResult<Task> {
                val wrappedRef = input.getInt("wrapped").getOrNull()
                    ?: return RichResult.failure("No wrapped task found")
                val wrapped = context.getTask(wrappedRef)
                    ?: return RichResult.failure($$"Cancellable$Savable task failed to create wrapped task: $$wrappedRef")
                val isCancelled = input.getBooleanOr("is_cancelled", false)

                val savable = Savable(wrapped)
                if (isCancelled) {
                    savable.cancel()
                }

                val onCancel = input.listOrEmpty("on_cancel", Codec.INT)
                for (onCancelRef in onCancel) {
                    val task = context.getTask(onCancelRef)
                        ?: return RichResult.failure($$"Cancellable$Savable task failed to create on_cancel task: $$onCancelRef")
                    savable.ifCancelled(task)
                }
                return RichResult.success(savable)
            }
        }
    }

    public companion object {
        /**
         * This method creates a cancellable task with a given runnable.
         *
         * If given a savable task this will save the savable task within
         * the cancellable task which will also be savable.
         *
         * @param task The task to wrap in a cancellable task.
         * @return The cancellable task.
         */
        @JvmStatic
        public fun of(task: Task): CancellableTask {
            return if (task is SavableTask || task is Serializable) Savable(task) else Default(task)
        }

        /**
         * This method creates a [CancellableTask] with a given runnable
         * similar to the [of] method *however* this method will also
         * make the runnable be called when the task is [ifCancelled].
         *
         * @param task The task to wrap in a cancellable task.
         * @return The cancellable task.
         * @see of
         */
        @JvmStatic
        public fun cancellable(task: Task): CancellableTask {
            return of(task).runIfCancelled()
        }
    }
}