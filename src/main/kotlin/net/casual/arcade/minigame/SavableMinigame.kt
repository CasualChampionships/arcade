package net.casual.arcade.minigame

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.scheduler.*
import net.casual.arcade.task.CancellableTask
import net.casual.arcade.task.MinigameTaskGenerator
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.intOrNull
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrDefault
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

/**
 * This extension of the [Minigame] class allows for serialization
 * of the minigame, in the case that the server is stopped, or if
 * the server crashes.
 *
 * This serializes everything that is part of the base minigame,
 * including the current phase, its uuid, scheduled tasks,
 * and the settings.
 * This class allows you to read and write additional custom data
 * for your specific minigame if there is data that needs to
 * be saved after a restart, see [readData] and [writeData].
 *
 * As mentioned scheduled tasks may be saved when the minigame is
 * saved however the task must implement [SavableTask], if the task
 * also implements [CancellableTask] the task must not be cancelled
 * to be serialized.
 *
 * If a single [SavableTask] object is scheduled multiple times
 * it will be serialized with its unique hash and when deserializing
 * only one object will be created to replace it.
 *
 * Furthermore, in order to read the tasks the [createTask] method
 * must be implemented which will create a task from a given
 * task id.
 * See [createTask] for more information.
 *
 * Important tasks that are integral to the running of the minigame
 * **should** be implemented as [SavableTask]s so they can be
 * reloaded if necessary.
 *
 * A very important note: when phases are read, they will be initialized
 * again, see [MinigamePhase.initialise] for more information.
 *
 * @param id The [ResourceLocation] of the [Minigame].
 * @param server The [MinecraftServer] that created the [Minigame].
 * @param path The path at which to read and write the minigame data.
 */
abstract class SavableMinigame(
    id: ResourceLocation,
    server: MinecraftServer,
    /**
     * The path at which to read and write the minigame data.
     */
    private val path: Path,
): Minigame(id, server) {
    /**
     * This method reads custom data for your minigame implementation
     * from your serialized [JsonObject].
     * Here you read all the data that you've serialized.
     *
     * While unlikely, you *should* consider irregular data that has
     * been modified by the user.
     *
     * @param json The [JsonObject] that was previously serialized.
     * @see writeData
     */
    @OverrideOnly
    protected abstract fun readData(json: JsonObject)

    /**
     * This method writes custom data for your minigame implementation
     * to a given [JsonObject].
     * Here you write all the data that you expect to deserialize.
     *
     * @param json The [JsonObject] that you are writing to.
     * @see readData
     */
    @OverrideOnly
    protected abstract fun writeData(json: JsonObject)

    /**
     * This method creates [Task]s from a given [id] and serialized [data].
     *
     * The task created should correspond to what was serialized.
     * If your minigame only has a couple tasks, then you can implement this
     * manually and check the [id]s.
     *
     * However, there is a utility class for generating tasks, see
     * [MinigameTaskGenerator], you can include this as a field in your
     * minigame and add all the factories you need to it in your constructor.
     *
     * For example:
     * ```kotlin
     * class MyMinigame: SavableMinigame(/* ... */) {
     *     private val generator = MinigameTaskGenerator(this)
     *
     *     init {
     *         this.generator.addFactory(/* ... */)
     *
     *         // ...
     *     }
     *
     *     override fun createTask(id: String, data: JsonObject): Task? {
     *         return this.generator.create(id, data)
     *     }
     *
     *     // ...
     * }
     * ```
     *
     * To see how you implement your task factories, see [SavableTask].
     *
     * If your task is not found null may be returned, however, this will
     * result in a warning error as this should **NOT** occur, you should
     * account for all the possible tasks that may be serialized.
     *
     * @param id The id of the task.
     * @param data The data written by the task.
     * @return The deserialized task; may be null if not found.
     * @see MinigameTaskGenerator
     * @see SavableTask
     */
    @OverrideOnly
    protected abstract fun createTask(id: String, data: JsonObject): Task?

    /**
     * This appends any additional debug information to [getDebugInfo].
     *
     * @param json The json append to.
     */
    override fun appendAdditionalDebugInfo(json: JsonObject) {
        super.appendAdditionalDebugInfo(json)
        json.addProperty("save_path", this.path.absolutePathString())
    }

    /**
     * This method initializes the core functionality of the
     * minigame, such as registering events.
     * This method should be called in your implementation's
     * constructor.
     */
    override fun initialise() {
        this.registerEvent<ServerSaveEvent> { this.save() }
        this.registerMinigameEvent<MinigameCloseEvent> { this.save() }

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
        if (json.has("uuid")) {
            this.uuid = UUID.fromString(json.string("uuid"))
        }

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
            display.setting.deserializeAndSetQuietly(value)
        }

        val custom = json.objOrNull("custom")
        if (custom != null) {
            this.readData(custom)
        }

        super.initialise()

        if (setPhase) {
            this.phase.initialise(this)
        } else {
            Arcade.logger.warn("Phase for minigame ${this.id} could not be reloaded, given phase id: $phaseId")
        }
    }

    private fun save() {
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
            data.add("value", setting.serializeValue())
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