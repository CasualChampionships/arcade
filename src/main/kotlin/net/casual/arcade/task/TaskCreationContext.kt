package net.casual.arcade.task

import com.google.gson.JsonObject

interface TaskCreationContext {
    fun getData(): JsonObject

    fun createTask(id: String, data: JsonObject): Task?
}