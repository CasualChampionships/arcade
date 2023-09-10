package net.casual.arcade.task

import com.google.gson.JsonObject

interface TaskCreationContext {
    fun getCustomData(): JsonObject

    fun createTask(data: JsonObject): Task?
}