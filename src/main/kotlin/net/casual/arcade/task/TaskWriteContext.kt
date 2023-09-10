package net.casual.arcade.task

import com.google.gson.JsonObject

interface TaskWriteContext {
    fun writeTask(task: Task): JsonObject?
}