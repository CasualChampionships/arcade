/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task

import com.google.gson.JsonObject
import net.casual.arcade.scheduler.task.serialization.TaskFactory
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface is used to represent a [Task] that
 * may be serialized.
 *
 * A [SavableTask] implementation should be complimented
 * by a [TaskFactory] or to be able to generate the task
 * when deserializing.
 *
 * The best way to do this is to have your companion object
 * inherit whichever factory interface you desire and
 * implementing it there:
 * ```kotlin
 * class MyTask: SavableTask {
 *     override val id = MyTask.id
 *
 *     // ...
 *
 *     override fun fun writeCustomData(context: TaskWriteContext): JsonObject {
 *         return JsonObject()
 *     }
 *
 *     companion object: TaskFactory {
 *         override val id: String = "my_task"
 *
 *         override fun create(context: TaskCreationContext): Task {
 *             return MyTask()
 *         }
 *     }
 * }
 * ```
 */
public interface SavableTask: Task {
    /**
     * The id for the task that will be serialized.
     */
    public val id: ResourceLocation

    /**
     * This writes any additional data needed
     * to create the implementation of this task.
     *
     * This serialized data will be passed to the
     * [TaskFactory] when generating a new task.
     *
     * @param context The [TaskSerializationContext].
     * @return The serialized data.
     */
    @OverrideOnly
    public fun serialize(context: TaskSerializationContext): JsonObject {
        return JsonObject()
    }
}


