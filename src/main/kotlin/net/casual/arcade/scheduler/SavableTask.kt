package net.casual.arcade.scheduler

import com.google.gson.JsonObject

interface SavableTask: Task {
    val id: String

    fun writeData(json: JsonObject) {

    }
}