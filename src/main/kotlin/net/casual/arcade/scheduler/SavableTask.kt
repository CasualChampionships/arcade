package net.casual.arcade.scheduler

import com.google.gson.JsonObject

abstract class SavableTask: Task() {
    abstract val id: String

    open fun writeData(json: JsonObject) {

    }
}