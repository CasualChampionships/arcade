package net.casualuhc.arcade.minigame

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.config.Config
import net.casualuhc.arcade.events.minigame.MinigameCloseEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.scheduler.SavableTask
import net.casualuhc.arcade.scheduler.Task
import net.casualuhc.arcade.utils.JsonUtils.array
import net.casualuhc.arcade.utils.JsonUtils.boolean
import net.casualuhc.arcade.utils.JsonUtils.int
import net.casualuhc.arcade.utils.JsonUtils.getObject
import net.casualuhc.arcade.utils.JsonUtils.objects
import net.casualuhc.arcade.utils.JsonUtils.string
import net.minecraft.resources.ResourceLocation
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

abstract class SavableMinigame(
    id: ResourceLocation,
    private val path: Path
): Minigame(id) {
    init {
        this.registerMinigameEvent<MinigameCloseEvent> { this.saveData() }
    }

    protected abstract fun readData(tag: JsonObject)

    protected abstract fun writeData(tag: JsonObject)

    protected abstract fun createTask(id: String, data: JsonObject): Task?

    fun loadData() {
        val json = if (this.path.exists()) {
            Config.GSON.fromJson(this.path.bufferedReader(), JsonObject::class.java)
        } else {
            JsonObject()
        }

        val phaseId = json.string("phase")
        for (phase in this.phases) {
            if (phase.id == phaseId) {
                this.phase = phase
                break
            }
        }
        this.paused = json.boolean("paused")

        val tasks = json.array("tasks")
        for (data in tasks.objects()) {
            val delay = data.int("delay")
            val id = data.string("id")
            val custom = data.getObject("custom")
            val task = this.createTask(id, custom)
            if (task !== null) {
                Arcade.logger.info("Successfully loaded task $id for minigame ${this.id}, scheduled for $delay ticks")
                this.schedulePhaseTask(delay, MinecraftTimeUnit.Ticks, task)
            } else {
                Arcade.logger.warn("Saved task $id for minigame ${this.id} could not be reloaded!")
            }
        }

        this.readData(json.getObject("custom"))
    }

    fun saveData() {
        val json = JsonObject()

        val tasks = JsonArray()
        for ((tick, queue) in this.scheduler.tasks) {
            val delay = tick - this.scheduler.tickCount
            for (task in queue) {
                if (task is SavableTask) {
                    val data = JsonObject()
                    val custom = JsonObject()
                    task.writeData(custom)
                    data.addProperty("delay", delay)
                    data.addProperty("name", task.id)
                    data.add("custom", custom)
                    tasks.add(data)
                }
            }
        }

        val custom = JsonObject()
        this.writeData(custom)

        json.add("custom", custom)
        json.add("tasks", tasks)

        Config.GSON.toJson(json, this.path.bufferedWriter())
    }
}