package net.casual.arcade.minigame

import com.google.common.collect.LinkedHashMultimap
import com.google.gson.JsonObject
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.minigame.commands.ExtendedGameModeCommand
import net.casual.arcade.minigame.commands.MinigameCommand
import net.casual.arcade.minigame.commands.PauseCommand
import net.casual.arcade.minigame.commands.TeamCommandModifier
import net.casual.arcade.minigame.exception.MinigameCreationException
import net.casual.arcade.minigame.exception.MinigameSerializationException
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.minigame.utils.MinigameUtils
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.casual.arcade.scheduler.task.utils.TaskRegisties
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.uuid
import net.fabricmc.api.ModInitializer
import net.minecraft.Util
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*
import kotlin.jvm.optionals.getOrNull

/**
 * This object is used for registering and holding
 * all the current minigames that are running.
 */
public object Minigames: ModInitializer {
    private val PATH = ArcadeUtils.path.resolve("minigames")

    private val ALL = LinkedHashMap<UUID, Minigame>()
    private val BY_ID = LinkedHashMultimap.create<ResourceLocation, Minigame>()

    /**
     * This method gets all the current running minigames.
     *
     * @return All the current running minigames.
     */
    public fun all(): Collection<Minigame> {
        return Collections.unmodifiableCollection(ALL.values)
    }

    /**
     * This gets the minigame that is associated with the given [UUID].
     *
     * @param uuid The uuid of the minigame.
     * @return The minigame with the given uuid.
     */
    public fun get(uuid: UUID): Minigame? {
        return this.ALL[uuid]
    }

    /**
     * This gets all the minigames that are associated with a given [ResourceLocation].
     *
     * @param id The id of the minigame.
     * @return All the minigames with the given id.
     */
    public fun get(id: ResourceLocation): List<Minigame> {
        return this.BY_ID.get(id).toList()
    }

    public fun create(
        id: ResourceLocation,
        context: MinigameCreationContext,
        data: Dynamic<*> = Dynamic(JsonOps.INSTANCE)
    ): Minigame {
        val codec = MinigameRegistries.MINIGAME_FACTORY.getOptional(id).getOrNull()
            ?: throw MinigameCreationException("Cannot create Minigame $id, no such factory found")
        val factory = codec.codec().parse(data).getOrThrow {
            MinigameCreationException("Failed to create Minigame $id with default factory parameters")
        }
        try {
            return factory.create(context)
        } catch (e: Exception) {
            throw MinigameCreationException("Failed to create Minigame $id", e)
        }
    }

    public fun read(path: Path, server: MinecraftServer): Minigame {
        val factoryPath = path.resolve("factory.json")
        if (!factoryPath.isRegularFile()) {
            throw MinigameCreationException("Cannot create Minigame, no such file $path")
        }

        val data = try {
            factoryPath.reader().use(JsonUtils::decodeToJsonObject)
        } catch (e: IOException) {
            throw MinigameCreationException("Cannot create Minigame, failed to read $path")
        }

        try {
            val factory = MinigameFactory.CODEC.parse(JsonOps.INSTANCE, data.obj("factory")).getOrThrow { message ->
                MinigameCreationException("Failed to decode minigame factory: $message")
            }
            val minigame = factory.create(MinigameCreationContext(server, data.uuid("uuid")))
            minigame.serialization.loadFrom(path)
            return minigame
        } catch (e: Exception) {
            throw MinigameCreationException("Failed to create Minigame for $path", e)
        }
    }

    public fun write(path: Path, minigame: Minigame) {
        val json = JsonObject()
        val factory = minigame.internalFactory() ?:
            throw MinigameSerializationException("Minigame ${minigame.id} is not serializable")

        val encoded = MinigameFactory.CODEC.encodeStart(JsonOps.INSTANCE, factory).getOrThrow { message ->
            MinigameSerializationException("Failed to serialize minigame factory for ${minigame.id}: $message")
        }
        json.add("factory", encoded)
        json.addProperty("uuid", minigame.uuid.toString())

        try {
            path.createDirectories()

            path.resolve("factory.json").writer().use { JsonUtils.encode(json, it) }
            minigame.serialization.saveTo(path)
        } catch (e: IOException) {
            throw MinigameSerializationException("Failed to write Minigame ${minigame.id} to $path", e)
        }
    }

    override fun onInitialize() {
        MinigameRegistries.load()
        MinigameUtils.registerEvents()
        ExtendedGameMode.registerEvents()

        GlobalEventHandler.Server.register<ServerLoadedEvent> { (server) ->
            this.loadMinigames(server)
        }
        GlobalEventHandler.Server.register<ServerSaveEvent> {
            this.saveMinigames()
        }
        GlobalEventHandler.Server.register<ServerStoppingEvent> {
            this.closeMinigames()
        }
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> { event ->
            event.register(ExtendedGameModeCommand, MinigameCommand, PauseCommand, TeamCommandModifier)
        }

        Registry.register(TaskRegisties.TASK_FACTORY, PhaseChangeTask.id, PhaseChangeTask)
        Registry.register(TaskRegisties.TASK_FACTORY, CancellableTask.Savable.id, CancellableTask.Savable)
    }

    internal fun allById(): Map<ResourceLocation, Collection<Minigame>> {
        return this.BY_ID.asMap()
    }

    internal fun register(minigame: Minigame) {
        this.ALL[minigame.uuid] = minigame
        this.BY_ID.put(minigame.id, minigame)
    }

    internal fun unregister(minigame: Minigame) {
        this.ALL.remove(minigame.uuid)
        this.BY_ID[minigame.id].remove(minigame)

        if (minigame.serializable) {
            Util.ioPool().execute {
                val path = this.getPathFor(minigame)
                if (path.exists()) {
                    @OptIn(ExperimentalPathApi::class)
                    path.deleteRecursively()
                }
            }
        }
    }

    private fun loadMinigames(server: MinecraftServer) {
        PATH.createDirectories()
        for (types in PATH.listDirectoryEntries()) {
            if (!types.isDirectory()) {
                continue
            }
            for (minigame in types.listDirectoryEntries()) {
                if (!minigame.isDirectory()) {
                    continue
                }
                try {
                    this.read(minigame, server)
                } catch (e: MinigameCreationException) {
                    ArcadeUtils.logger.error(e)
                }
            }
        }
    }

    private fun saveMinigames() {
        for (minigame in ALL.values) {
            if (minigame.serializable) {
                try {
                    this.write(this.getPathFor(minigame), minigame)
                } catch (e: MinigameSerializationException) {
                    ArcadeUtils.logger.error(e)
                }
            }
        }
    }

    private fun closeMinigames() {
        for (minigame in ArrayList(ALL.values)) {
            if (!minigame.serializable) {
                minigame.close()
            }
        }
    }

    private fun getPathFor(minigame: Minigame): Path {
        return PATH.resolve("${minigame.id.namespace}.${minigame.id.path}").resolve(minigame.uuid.toString())
    }
}