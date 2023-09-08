package net.casual.arcade.minigame

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.scheduler.*
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.intOrNull
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrDefault
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import java.nio.file.Path
import java.util.*
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

abstract class SavableMinigame(
    id: ResourceLocation,
    server: MinecraftServer,
    private val path: Path,
): Minigame(id, server) {
    init {
        this.registerEvent<ServerSaveEvent> { this.save() }
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
        val json = this.path.bufferedReader().use {
            CustomisableConfig.GSON.fromJson(it, JsonObject::class.java)
        }

        val phaseId = json.stringOrDefault("phase")
        var setPhase = false
        for (phase in this.phases) {
            if (phase.id == phaseId) {
                this.phase = phase
                setPhase = true
                break
            }
        }
        this.paused = json.booleanOrDefault("paused")
        this.uuid = UUID.fromString(json.string("uuid"))

        val generated = HashMap<Int, Task?>()

        for (task in json.arrayOrDefault("tasks").objects()) {
            this.readScheduledTask(task, this.scheduler, generated)
        }
        for (task in json.arrayOrDefault("phase_tasks").objects()) {
            this.readScheduledTask(task, this.phaseScheduler, generated)
        }

        for (data in json.arrayOrDefault("phase_end_tasks").objects()) {
            val (id, task) = this.readTask(data, generated)
            if (task !== null) {
                Arcade.logger.info("Successfully loaded phase end task $id for minigame ${this.id}")
                this.schedulePhaseEndTask(task)
            } else {
                Arcade.logger.warn("Saved task $id for minigame ${this.id} could not be reloaded!")
            }
        }
        generated.clear()

        for (data in json.arrayOrDefault("settings").objects()) {
            val name = data.string("name")
            val value = data.get("value")
            val display = this.gameSettings[name]
            if (display == null) {
                Arcade.logger.warn("Saved setting $name for minigame ${this.id} could not be reloaded")
                continue
            }
            display.setting.deserialiseAndSetQuietly(value)
        }

        this.readData(json.obj("custom"))

        super.initialise()

        if (setPhase) {
            this.phase.initialise(this)
        } else {
            Arcade.logger.warn("Phase for minigame ${this.id} could not be reloaded, given phase id: $phaseId")
        }
    }

    fun save() {
        val json = JsonObject()

        json.addProperty("phase", this.phase.id)
        json.addProperty("paused", this.paused)
        json.addProperty("uuid", this.uuid.toString())

        val tasks = this.writeScheduledTasks(this.scheduler)
        val phaseTasks = this.writeScheduledTasks(this.phaseScheduler)

        val phaseEndTasks = JsonArray()
        for (task in this.phaseEndTasks) {
            phaseTasks.add(this.writeTask(task) ?: continue)
        }

        val settings = JsonArray()
        for (setting in this.getSettings()) {
            val data = JsonObject()
            data.addProperty("name", setting.name)
            data.add("value", setting.serialiseValue())
            settings.add(data)
        }

        val custom = JsonObject()
        this.writeData(custom)

        json.add("tasks", tasks)
        json.add("phase_tasks", phaseTasks)
        json.add("phase_end_tasks", phaseEndTasks)
        json.add("settings", settings)
        json.add("custom", custom)

        this.path.bufferedWriter().use {
            CustomisableConfig.GSON.toJson(json, it)
        }
    }

    private fun readScheduledTask(json: JsonObject, scheduler: TickedScheduler, generated: MutableMap<Int, Task?>) {
        val delay = json.int("delay")
        val (id, task) = this.readTask(json, generated)
        if (task !== null) {
            Arcade.logger.info("Successfully loaded task $id for minigame ${this.id}, scheduled for $delay ticks")
            scheduler.schedule(delay, MinecraftTimeUnit.Ticks, task)
        } else {
            Arcade.logger.warn("Saved task $id for minigame ${this.id} could not be reloaded!")
        }
    }

    private fun writeScheduledTasks(scheduler: TickedScheduler): JsonArray {
        val tasks = JsonArray()
        for ((tick, queue) in scheduler.tasks) {
            val delay = tick - scheduler.tickCount
            for (task in queue) {
                val written = this.writeTask(task) ?: continue
                written.addProperty("delay", delay)
                tasks.add(written)
            }
        }
        return tasks
    }

    private fun readTask(json: JsonObject, generated: MutableMap<Int, Task?>): Pair<String, Task?> {
        val id = json.string("id")
        val hash = json.intOrNull("hash")
        val custom = json.obj("custom")
        if (hash == null) {
            return id to this.createTask(id, custom)
        }
        return id to generated.getOrPut(hash) { this.createTask(id, custom) }
    }

    private fun writeTask(task: Task): JsonObject? {
        if (task is SavableTask && !(task is CancellableTask && task.isCancelled())) {
            val data = JsonObject()
            val custom = JsonObject()
            task.writeData(custom)
            data.addProperty("id", task.id)
            data.addProperty("hash", System.identityHashCode(task))
            data.add("custom", custom)
            return data
        }
        return null
    }
}