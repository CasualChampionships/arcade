package net.casual.arcade.minigame.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import net.casual.arcade.Arcade
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.task.AnyMinigameTaskFactory
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.minigame.task.MinigameTaskGenerator
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.task.impl.CancellableTask
import net.casual.arcade.task.serialization.TaskCreationContext
import net.casual.arcade.task.serialization.TaskFactory
import net.casual.arcade.task.serialization.TaskWriteContext
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.hasNonNull
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.intOrNull
import net.casual.arcade.utils.JsonUtils.objOrDefault
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrDefault
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.StringUtils.decodeHexToBytes
import net.casual.arcade.utils.StringUtils.encodeToHexString
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus.Internal
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.io.ByteArrayOutputStream
import java.io.NotSerializableException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

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
 * saved however the task must implement [SavableTask], regular
 * [Task]s **may** be saved but this is not guaranteed.
 * This behaviour depends on whether the closure of the task
 * is also serializable. If you have a task that requires capturing
 *
 * If a single [SavableTask] object is scheduled multiple times
 * it will be serialized with its unique hash and when deserializing
 * only one object will be created to replace it.
 *
 * Furthermore, to read the tasks you must add [TaskFactory]s
 * to your minigame.
 * See [addTaskFactory].
 *
 * Important tasks that are integral to the running of the minigame
 * **should** be implemented as [SavableTask]s so they can be
 * reloaded if necessary.
 *
 * A crucial note: when phases are read, they will be initialized
 * again, see [MinigamePhase.initialize] for more information.
 *
 * @param M The type of the child class.
 * @param server The [MinecraftServer] that created the [Minigame].
 * @see Minigame
 */
public abstract class SavableMinigame<M: SavableMinigame<M>>(
    server: MinecraftServer
): Minigame<M>(server) {
    private val taskGenerator = MinigameTaskGenerator(this.cast())

    init {
        // Add default task factories
        this.addTaskFactory(CancellableTask.Savable)
        this.addTaskFactory(PhaseChangeTask)
    }

    /**
     * This adds a task factory to your minigame, so it is
     * able to deserialize tasks.
     * Task factories should be added in your constructor.
     *
     * See [SavableTask] for details on how you should implement
     * your task factories.
     *
     * @param factory The task factory to add.
     * @see SavableTask
     */
    protected fun addTaskFactory(factory: TaskFactory) {
        this.taskGenerator.addFactory(factory)
    }

    /**
     * This adds a task factory to your minigame, so it is
     * able to deserialize tasks.
     * Task factories should be added in your constructor.
     *
     * See [SavableTask] for details on how you should implement
     * your task factories.
     *
     * @param factory The task factory to add.
     * @see SavableTask
     */
    protected fun addTaskFactory(factory: MinigameTaskFactory<M>) {
        return this.taskGenerator.addFactory(factory)
    }

    /**
     * This adds a task factory to your minigame, so it is
     * able to deserialize tasks.
     * Task factories should be added in your constructor.
     *
     * See [SavableTask] for details on how you should implement
     * your task factories.
     *
     * @param factory The task factory to add.
     * @see SavableTask
     */
    protected fun addTaskFactory(factory: AnyMinigameTaskFactory) {
        this.addTaskFactory(factory.cast())
    }

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

    @Internal
    public fun read(json: JsonObject): Boolean {
        val phaseId = json.stringOrNull("phase")
        var setPhase = false
        if (phaseId != null) {
            for (phase in this.phases) {
                if (phase.id == phaseId) {
                    this.phase = phase
                    setPhase = true
                    break
                }
            }
        }

        this.paused = json.booleanOrDefault("paused")
        if (json.has("uuid")) {
            Minigames.unregister(this)
            this.uuid = UUID.fromString(json.string("uuid"))
            Minigames.register(this)
        }

        val generated = HashMap<Int, Task?>()
        for (task in json.arrayOrDefault("tasks").objects()) {
            this.readScheduledTask(task, this.scheduler.minigame, generated)
        }
        for (task in json.arrayOrDefault("phase_tasks").objects()) {
            this.readScheduledTask(task, this.scheduler.phased, generated)
        }
        generated.clear()

        for (player in json.arrayOrDefault("players").objects()) {
            val uuid = if (player.hasNonNull("uuid")) UUID.fromString(player.string("uuid")) else null
            this.offline.add(GameProfile(uuid, player.stringOrNull("name")))
        }

        for (spectator in json.arrayOrDefault("spectators").strings()) {
            this.spectators.add(UUID.fromString(spectator))
        }

        for (admin in json.arrayOrDefault("admins").strings()) {
            this.admins.add(UUID.fromString(admin))
        }

        this.settings.deserialize(json.arrayOrDefault("settings"))
        this.stats.deserialize(json.arrayOrDefault("stats"))

        val custom = json.objOrNull("custom")
        if (custom != null) {
            this.readData(custom)
        }

        if (setPhase) {
            this.phase.initialize(this.cast())
        } else {
            Arcade.logger.warn("Phase for minigame ${this.id} could not be reloaded, given phase id: $phaseId")
        }
        return true
    }

    @Internal
    public fun save(): JsonObject {
        val json = JsonObject()

        json.addProperty("phase", this.phase.id)
        json.addProperty("paused", this.paused)
        json.addProperty("uuid", this.uuid.toString())

        val tasks = this.writeScheduledTasks(this.scheduler.minigame)
        val phaseTasks = this.writeScheduledTasks(this.scheduler.phased)

        val players = JsonArray()
        for (player in this.getAllPlayerProfiles()) {
            val data = JsonObject()
            data.addProperty("name", player.name)
            data.addProperty("uuid", player.id?.toString())
            players.add(data)
        }

        val spectators = JsonArray()
        for (spectator in this.spectators) {
            spectators.add(spectator.toString())
        }

        val admins = JsonArray()
        for (admin in this.admins) {
            admins.add(admin.toString())
        }

        val settings = this.settings.serialize()
        val stats = this.stats.serialize()

        val custom = JsonObject()
        this.writeData(custom)

        json.add("tasks", tasks)
        json.add("phase_tasks", phaseTasks)
        json.add("players", players)
        json.add("spectators", spectators)
        json.add("admins", admins)
        json.add("settings", settings)
        json.add("stats", stats)
        json.add("custom", custom)
        return json
    }

    private fun readScheduledTask(json: JsonObject, scheduler: TickedScheduler, generated: MutableMap<Int, Task?>) {
        val delay = json.int("delay")
        val task = this.deserializeTask(json, generated)
        val id = json.stringOrDefault("id", "[Anonymous]")
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
                val written = this.serializeTask(task) ?: continue
                written.addProperty("delay", delay)
                tasks.add(written)
            }
        }
        return tasks
    }

    private fun deserializeTask(json: JsonObject, generated: MutableMap<Int, Task?>): Task? {
        val hash = json.intOrNull("hash")
        val generator = this.createTaskGenerator(json, generated) ?: return null
        if (hash == null) {
            return generator()
        }
        return generated.getOrPut(hash, generator)
    }

    private fun createTaskGenerator(
        json: JsonObject,
        generated: MutableMap<Int, Task?>
    ): (() -> Task?)? {
        if (json.has("raw")) {
            return {
                json.string("raw").decodeHexToBytes().inputStream().use { bytes ->
                    ObjectInputStream(bytes).use { it.readObject() as Task }
                }
            }
        }
        val id = json.stringOrNull("id") ?: return null
        val custom = json.objOrDefault("custom")
        return { this.taskGenerator.generate(id, MinigameTaskCreationContext(custom, generated)) }
    }

    private fun serializeTask(task: Task): JsonObject? {
        if (task is SavableTask) {
            val data = JsonObject()
            val result = this.runCatching { task.writeCustomData(MinigameTaskWriteContext()) }
            if (!result.isFailure) {
                data.addProperty("id", task.id)
                data.addProperty("hash", System.identityHashCode(task))
                data.add("custom", result.getOrThrow())
                return data
            }
        }
        try {
            ByteArrayOutputStream().use { bytes ->
                ObjectOutputStream(bytes).use { stream ->
                    stream.writeObject(task)
                }
                val data = JsonObject()
                data.addProperty("hash", System.identityHashCode(task))
                data.addProperty("raw", bytes.toByteArray().encodeToHexString())
                return data
            }
        } catch (ignored: NotSerializableException) {

        }
        Arcade.logger.warn("Savable minigame scheduled a non-savable task, skipping it...")
        return null
    }

    private inner class MinigameTaskCreationContext(
        private val data: JsonObject,
        private val generated: MutableMap<Int, Task?>,
    ): TaskCreationContext {
        override fun getCustomData(): JsonObject {
            return this.data
        }

        override fun createTask(data: JsonObject): Task? {
            return deserializeTask(data, this.generated)
        }
    }

    private inner class MinigameTaskWriteContext: TaskWriteContext {
        override fun writeTask(task: Task): JsonObject? {
            return serializeTask(task)
        }
    }
}