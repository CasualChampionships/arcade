package net.casual.arcade.minigame.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.Arcade
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.AnyMinigameTaskFactory
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.minigame.task.MinigameTaskGenerator
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.task.impl.CancellableTask
import net.casual.arcade.task.serialization.TaskCreationContext
import net.casual.arcade.task.serialization.TaskCreationContext.Companion.withCustomData
import net.casual.arcade.task.serialization.TaskFactory
import net.casual.arcade.task.serialization.TaskWriteContext
import net.casual.arcade.utils.FastUtils.component1
import net.casual.arcade.utils.FastUtils.component2
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.intOrDefault
import net.casual.arcade.utils.JsonUtils.objOrDefault
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.casual.arcade.utils.JsonUtils.toJsonArray
import net.casual.arcade.utils.JsonUtils.uuidOrDefault
import net.casual.arcade.utils.JsonUtils.uuidOrNull
import net.casual.arcade.utils.JsonUtils.uuids
import net.casual.arcade.utils.MinigameUtils.getPhase
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.Util
import net.minecraft.server.MinecraftServer
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamException
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.reader
import kotlin.io.path.writer

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
 * be saved after a restart, see [loadData] and [saveData].
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
 * again, see [Phase.initialize] for more information.
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
     * This method reads custom runtime data for your minigame
     * implementation from your serialized [JsonObject].
     * Here you read all the data that you've serialized.
     *
     * While unlikely, you *should* consider irregular data that has
     * been modified by the user.
     *
     * @param json The [JsonObject] that was previously serialized.
     * @see saveData
     */
    @OverrideOnly
    protected abstract fun loadData(json: JsonObject)

    /**
     * This method writes custom runtime data for your minigame
     * implementation to a given [JsonObject].
     * Here you write all the data that you expect to deserialize.
     *
     * @param json The [JsonObject] that you are writing to.
     * @see loadData
     */
    @OverrideOnly
    protected abstract fun saveData(json: JsonObject)

    /**
     * This method saves any parameters that would be passed into
     * this minigames [MinigameFactory] to re-construct
     * this instance of this minigame.
     *
     * @param json The [JsonObject] to write to.
     *
     * @see MinigameCreationContext.parameters
     */
    @OverrideOnly
    protected open fun saveParameters(json: JsonObject) {

    }

    internal fun loadFrom(path: Path, core: JsonObject) {
        val (initialized, phaseSet) = this.readMinigameJson(core)

        this.readAsJsonObjectInto(path.resolve("tasks.json"), this::readTasksJson)
        this.readAsJsonObjectInto(path.resolve("players.json"), this::readPlayersJson)
        this.readAsJsonObjectInto(path.resolve("chat_manager.json"), this.chat::deserialize)
        this.readAsJsonArrayInto(path.resolve("settings.json"), this.settings::deserialize)
        this.readAsJsonArrayInto(path.resolve("stats.json"), this.stats::deserialize)
        this.readAsJsonArrayInto(path.resolve("tags.json"), this.tags::deserialize)
        this.readAsJsonArrayInto(path.resolve("recipes.json"), this.recipes::deserialize)
        this.readAsJsonObjectInto(path.resolve("data_tracker.json"), this.data::deserialize)
        this.readAsJsonObjectInto(path.resolve("custom.json"), this::loadData)

        if (initialized) {
            this.tryInitialize()
        }
        if (phaseSet) {
            // Ordered phases
            for (phase in this.phases) {
                if (phase <= this.phase) {
                    phase.initialize(this.cast())
                }
            }
        } else {
            Arcade.logger.warn("Phase for minigame ${this.id} could not be reloaded")
        }
    }

    internal fun saveTo(path: Path) {
        this.writeAsyncAsJsonElementInto(path.resolve("minigame.json"), this::writeMinigameJson)

        this.writeAsyncAsJsonElementInto(path.resolve("tasks.json"), this::writeTasksJson)
        this.writeAsyncAsJsonElementInto(path.resolve("players.json"), this::writePlayerJson)
        this.writeAsyncAsJsonElementInto(path.resolve("chat_manager.json"), this.chat::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("settings.json"), this.settings::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("stats.json"), this.stats::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("tags.json"), this.tags::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("recipes.json"), this.recipes::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("data_tracker.json"), this.data::serialize)

        this.writeAsyncAsJsonElementInto(path.resolve("custom.json"), this::writeCustomJson)
    }

    private inline fun readAsJsonObjectInto(path: Path, block: (JsonObject) -> Unit) {
        if (path.isRegularFile()) {
            block.invoke(path.reader().use(JsonUtils::decodeToJsonObject))
        }
    }

    private inline fun readAsJsonArrayInto(path: Path, block: (JsonArray) -> Unit) {
        if (path.isRegularFile()) {
            block.invoke(path.reader().use(JsonUtils::decodeToJsonArray))
        }
    }

    private fun readMinigameJson(json: JsonObject): BooleanBooleanPair {
        val initialized = json.booleanOrDefault("initialized")
        this.started = json.booleanOrDefault("started")

        val phaseId = json.stringOrNull("phase")
        var phaseSet = false
        if (phaseId != null) {
            val phase = this.getPhase(phaseId)
            if (phase != null) {
                this.phase = phase
                phaseSet = true
            }
        }

        this.uptime = json.intOrDefault("uptime")
        this.paused = json.booleanOrDefault("paused")
        this.uuid = json.uuidOrDefault("uuid", this.uuid)

        return BooleanBooleanPair.of(initialized, phaseSet)
    }

    private fun readTasksJson(json: JsonObject) {
        val definitions = Int2ObjectOpenHashMap<JsonObject>()
        for (definition in json.arrayOrDefault("task_definitions").objects()) {
            definitions.put(definition.int("uid"), definition)
        }
        val context = MinigameTaskCreationContext(JsonObject(), definitions, Int2ObjectOpenHashMap())
        this.readScheduledTasks(json.arrayOrDefault("scheduled_tasks"), this.scheduler.minigame, context)
        this.readScheduledTasks(json.arrayOrDefault("scheduled_phase_tasks"), this.scheduler.phased, context)
        context.clear()
    }

    private fun readPlayersJson(json: JsonObject) {
        this.teams.deserialize(json.objOrDefault("teams"), this.server.scoreboard)

        for (player in json.arrayOrDefault("players").objects()) {
            this.players.offlineGameProfiles.add(GameProfile(player.uuidOrNull("uuid"), player.stringOrNull("name")))
        }

        this.players.spectatorUUIDs.addAll(json.arrayOrDefault("spectators").uuids())
        this.players.adminUUIDs.addAll(json.arrayOrDefault("admins").uuids())
    }

    private fun readScheduledTasks(tasks: JsonArray, scheduler: TickedScheduler, context: MinigameTaskCreationContext) {
        for (data in tasks.objects()) {
            val ticks = data.int("delay")
            val identity = data.int("uid")
            val task = this.deserializeTask(identity, context)
            if (task != null) {
                scheduler.schedule(ticks.Ticks, task)
            } else {
                Arcade.logger.warn("Saved task $identity for minigame ${this.id} could not be reloaded!")
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun deserializeTask(identity: Int, context: MinigameTaskCreationContext): Task? {
        if (context.generated.containsKey(identity)) {
            return context.generated.get(identity)
        }

        val definition = context.definitions.get(identity) ?: return null
        val task = if (definition.has("raw")) {
            try {
                definition.string("raw").hexToByteArray().inputStream().use { bytes ->
                    ObjectInputStream(bytes).use { it.readObject() as Task }
                }
            } catch (_: ObjectStreamException) {
                null
            }
        } else {
            val id = definition.stringOrNull("id") ?: return null
            val custom = definition.objOrDefault("custom")
            this.taskGenerator.generate(id, context.withCustomData(custom))
        }
        context.generated.put(identity, task)
        return task
    }

    private inline fun writeAsyncAsJsonElementInto(path: Path, block: () -> JsonElement) {
        val json = block.invoke()
        Util.ioPool().execute {
            path.writer().use {
                JsonUtils.encode(json, it)
            }
        }
    }

    private fun writeMinigameJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("id", this.id.toString())
        json.addProperty("initialized", this.initialized)
        json.addProperty("started", this.started)
        json.addProperty("phase", this.phase.id)
        json.addProperty("uptime", this.uptime)
        json.addProperty("paused", this.paused)
        json.addProperty("uuid", this.uuid.toString())
        val parameters = JsonObject()
        this.saveParameters(parameters)
        json.add("parameters", parameters)
        return json
    }

    private fun writeTasksJson(): JsonObject {
        val json = JsonObject()
        val context = MinigameTaskWriteContext(Int2ObjectOpenHashMap())
        json.add("scheduled_tasks", this.writeScheduledTasks(this.scheduler.minigame, context))
        json.add("scheduled_phase_tasks", this.writeScheduledTasks(this.scheduler.phased, context))
        json.add("task_definitions", context.definitions.values.toJsonArray())
        context.clear()
        return json
    }

    private fun writePlayerJson(): JsonObject {
        val json = JsonObject()
        val players = JsonArray()
        for (player in this.players.allProfiles) {
            val data = JsonObject()
            data.addProperty("name", player.name)
            data.addProperty("uuid", player.id?.toString())
            players.add(data)
        }

        val spectators = JsonArray()
        for (spectator in this.players.spectatorUUIDs) {
            spectators.add(spectator.toString())
        }

        val admins = JsonArray()
        for (admin in this.players.adminUUIDs) {
            admins.add(admin.toString())
        }
        json.add("teams", this.teams.serialize())
        json.add("players", players)
        json.add("spectators", spectators)
        json.add("admins", admins)
        return json
    }

    private fun writeCustomJson(): JsonObject {
        val custom = JsonObject()
        this.saveData(custom)
        return custom
    }

    private fun writeScheduledTasks(
        scheduler: TickedScheduler,
        context: MinigameTaskWriteContext
    ): JsonArray {
        val tasks = JsonArray()
        for ((tick, queue) in scheduler.tasks) {
            val delay = tick - scheduler.tickCount
            for (task in queue) {
                val identity = this.serializeTask(task, context) ?: continue
                val data = JsonObject()
                data.addProperty("uid", identity)
                data.addProperty("delay", delay)
                tasks.add(data)
            }
        }
        return tasks
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun serializeTask(task: Task, context: MinigameTaskWriteContext): Int? {
        val identity = System.identityHashCode(task)
        if (context.definitions.containsKey(identity)) {
            return identity
        }

        var exception: Exception? = null
        if (task is SavableTask) {
            try {
                val definition = JsonObject()
                definition.addProperty("id", task.id)
                definition.addProperty("uid", identity)
                definition.add("custom", task.writeCustomData(context))
                context.definitions.put(identity, definition)
                return identity
            } catch (e: Exception) {
                exception = e
            }
        }

        try {
            ByteArrayOutputStream().use { bytes ->
                ObjectOutputStream(bytes).use { stream ->
                    stream.writeObject(task)
                }
                val definition = JsonObject()
                definition.addProperty("uid", identity)
                definition.addProperty("raw", bytes.toByteArray().toHexString())
                context.definitions.put(identity, definition)
                return identity
            }
        } catch (ignored: ObjectStreamException) {

        }
        Arcade.logger.warn("Failed to serialize non-savable task, skipping it...", exception)
        return null
    }

    private inner class MinigameTaskCreationContext(
        private val data: JsonObject,
        val definitions: Int2ObjectMap<JsonObject>,
        val generated: Int2ObjectMap<Task?>,
    ): TaskCreationContext {
        override fun getCustomData(): JsonObject {
            return this.data
        }

        override fun createTask(uid: Int): Task? {
            return deserializeTask(uid, this)
        }

        fun clear() {
            this.definitions.clear()
            this.generated.clear()
        }
    }

    private inner class MinigameTaskWriteContext(
        val definitions: Int2ObjectMap<JsonObject>
    ): TaskWriteContext {
        override fun writeTask(task: Task): Int? {
            return serializeTask(task, this)
        }

        fun clear() {
            this.definitions.clear()
        }
    }
}