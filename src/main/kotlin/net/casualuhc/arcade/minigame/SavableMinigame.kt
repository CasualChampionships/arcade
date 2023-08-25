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
import net.minecraft.server.MinecraftServer
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

abstract class SavableMinigame(
    id: ResourceLocation,
    server: MinecraftServer,
    private val path: Path,
): Minigame(id, server) {
    init {
        this.registerMinigameEvent<MinigameCloseEvent> { this.save() }
    }

    protected abstract fun readData(json: JsonObject)

    protected abstract fun writeData(json: JsonObject)

    protected abstract fun createTask(id: String, data: JsonObject): Task?

    override fun initialise() {
        if (!this.path.exists()) {
            super.initialise()
            return
        }
        val json = Config.GSON.fromJson(this.path.bufferedReader(), JsonObject::class.java)

        val phaseId = json.string("phase")
        for (phase in this.phases) {
            if (phase.id == phaseId) {
                this.phase = phase
                break
            }
        }
        this.paused = json.boolean("paused")
        this.uuid = UUID.fromString(json.string("uuid"))

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

        val endTasks = json.array("end_tasks")
        for (data in endTasks.objects()) {
            val id = data.string("id")
            val custom = data.getObject("custom")
            val task = this.createTask(id, custom)
            if (task !== null) {
                Arcade.logger.info("Successfully loaded end task $id for minigame ${this.id}")
                this.schedulePhaseEndTask(task)
            } else {
                Arcade.logger.warn("Saved task $id for minigame ${this.id} could not be reloaded!")
            }
        }

        val settings = json.array("settings")
        for (data in settings.objects()) {
            val name = data.string("name")
            val value = data.get("value")
            val display = this.settings[name]
            if (display == null) {
                Arcade.logger.warn("Saved setting $name for minigame ${this.id} could not be reloaded")
                continue
            }
            display.setting.deserialise(value)
        }

        this.readData(json.getObject("custom"))

        super.initialise()
    }

    fun save() {
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

        val endTasks = JsonArray()
        for (task in this.tasks) {
            if (task is SavableTask) {
                val data = JsonObject()
                val custom = JsonObject()
                task.writeData(custom)
                data.addProperty("name", task.id)
                data.add("custom", custom)
                endTasks.add(data)
            }
        }

        val settings = JsonArray()
        for (setting in this.getSettings()) {
            val data = JsonObject()
            data.addProperty("name", setting.name)
            data.add("value", setting.serialise())
            settings.add(data)
        }

        val custom = JsonObject()
        this.writeData(custom)

        json.add("custom", custom)
        json.add("tasks", tasks)
        json.add("end_tasks", endTasks)
        json.add("settings", settings)

        Config.GSON.toJson(json, this.path.bufferedWriter())
    }
}