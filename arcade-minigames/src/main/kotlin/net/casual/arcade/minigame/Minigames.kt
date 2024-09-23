package net.casual.arcade.minigame

import com.google.common.collect.LinkedHashMultimap
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.minigame.commands.ExtendedGameModeCommand
import net.casual.arcade.minigame.commands.MinigameCommand
import net.casual.arcade.minigame.commands.PauseCommand
import net.casual.arcade.minigame.commands.TeamCommandModifier
import net.casual.arcade.minigame.exception.MinigameCreationException
import net.casual.arcade.minigame.exception.MinigameSerializationException
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.minigame.template.lobby.LobbyTemplate
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.minigame.utils.MinigameUtils
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.objOrDefault
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.ResourceUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.Util
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import java.io.IOException
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

/**
 * This object is used for registering and holding
 * all the current minigames that are running.
 */
public object Minigames: ModInitializer {
    private val PATH = ArcadeUtils.path.resolve("minigames")

    private val ALL = LinkedHashMap<UUID, Minigame<*>>()
    private val BY_ID = LinkedHashMultimap.create<ResourceLocation, Minigame<*>>()
    private val FACTORIES_BY_ID = LinkedHashMap<ResourceLocation, MinigameFactory>()

    /**
     * This method gets all the current running minigames.
     *
     * @return All the current running minigames.
     */
    public fun all(): Collection<Minigame<*>> {
        return Collections.unmodifiableCollection(ALL.values)
    }

    /**
     * This registers a [MinigameFactory] for your minigame.
     *
     * You should do this if your minigame is a [SavableMinigame] as
     * it will allow for the minigame to automatically restart when
     * the server reboots.
     *
     * Minigames are read from a json file when the server has loaded,
     * typically you want to register your factory in your mod initializer.
     *
     * Factories also allow you to dynamically create minigames at runtime.
     *
     * @param id The id of the minigame that will be created.
     * @param factory The factory that will make minigames.
     */
    public fun registerFactory(id: ResourceLocation, factory: MinigameFactory) {
        this.FACTORIES_BY_ID[id] = factory
    }

    /**
     * This method gets a [MinigameFactory] by its id.
     *
     * @param id The id of the factory.
     * @return The factory with the given id.
     */
    public fun getFactory(id: ResourceLocation): MinigameFactory? {
        return this.FACTORIES_BY_ID[id]
    }

    /**
     * This method gets all the ids of the registered factories.
     *
     * @return All the ids of the registered factories.
     */
    public fun getAllFactoryIds(): MutableSet<ResourceLocation> {
        return this.FACTORIES_BY_ID.keys
    }

    /**
     * This gets the minigame that is associated with the given [UUID].
     *
     * @param uuid The uuid of the minigame.
     * @return The minigame with the given uuid.
     */
    public fun get(uuid: UUID): Minigame<*>? {
        return this.ALL[uuid]
    }

    /**
     * This gets all the minigames that are associated with a given [ResourceLocation].
     *
     * @param id The id of the minigame.
     * @return All the minigames with the given id.
     */
    public fun get(id: ResourceLocation): List<Minigame<*>> {
        return this.BY_ID.get(id).toList()
    }

    public fun create(id: ResourceLocation, context: MinigameCreationContext): Minigame<*> {
        val factory = FACTORIES_BY_ID[id]
            ?: throw MinigameCreationException("Cannot create Minigame $id, no such factory found")
        try {
            return factory.create(context)
        } catch (e: Exception) {
            throw MinigameCreationException("Failed to create Minigame $id", e)
        }
    }

    public fun read(path: Path, server: MinecraftServer): Minigame<*> {
        val minigamePath = path.resolve("minigame.json")
        if (!minigamePath.isRegularFile()) {
            throw MinigameCreationException("Cannot create Minigame, no such file $path")
        }

        val core = try {
            minigamePath.reader().use(JsonUtils::decodeToJsonObject)
        } catch (e: IOException) {
            throw MinigameCreationException("Cannot create Minigame, failed to read $path")
        }
        val rawId = core.string("id")
        val id = ResourceLocation.tryParse(rawId)
            ?: throw MinigameCreationException("Cannot create Minigame $rawId, invalid ResourceLocation")
        val factory = FACTORIES_BY_ID[id]
            ?: throw MinigameCreationException("Cannot create Minigame $id, no such factory found")

        try {
            val minigame = factory.create(MinigameCreationContext(server, core.objOrDefault("parameters")))
            if (minigame !is SavableMinigame) {
                throw MinigameCreationException("Factory created non-savable Minigame $id")
            }
            minigame.loadFrom(path, core)
            return minigame
        } catch (e: Exception) {
            throw MinigameCreationException("Failed to create Minigame $id", e)
        }
    }

    public fun write(path: Path, minigame: SavableMinigame<*>) {
        try {
            path.createDirectories()
            minigame.saveTo(path)
        } catch (e: IOException) {
            throw MinigameSerializationException("Failed to write Minigame ${minigame.id} to $path", e)
        }
    }

    override fun onInitialize() {
        MinigameRegistries.load()
        MinigameUtils.registerEvents()
        ExtendedGameMode.registerEvents()

        GlobalEventHandler.register<ServerLoadedEvent> { (server) ->
            this.loadMinigames(server)
        }
        GlobalEventHandler.register<ServerSaveEvent> {
            this.saveMinigames()
        }
        GlobalEventHandler.register<ServerRegisterCommandEvent> { event ->
            event.register(ExtendedGameModeCommand, MinigameCommand, PauseCommand, TeamCommandModifier)
        }

        registerFactory(ResourceUtils.arcade("lobby")) {
            LobbyMinigame(it.server, LobbyTemplate.DEFAULT.create(it.server.overworld()))
        }
    }

    internal fun allById(): Map<ResourceLocation, Collection<Minigame<*>>> {
        return this.BY_ID.asMap()
    }

    internal fun register(minigame: Minigame<*>) {
        this.ALL[minigame.uuid] = minigame
        this.BY_ID.put(minigame.id, minigame)
    }

    internal fun unregister(minigame: Minigame<*>) {
        this.ALL.remove(minigame.uuid)
        this.BY_ID[minigame.id].remove(minigame)

        if (minigame is SavableMinigame) {
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
            if (minigame !is SavableMinigame) {
                continue
            }
            try {
                this.write(this.getPathFor(minigame), minigame)
            } catch (e: MinigameSerializationException) {
                ArcadeUtils.logger.error(e)
            }
        }
    }

    private fun getPathFor(minigame: Minigame<*>): Path {
        return PATH.resolve("${minigame.id.namespace}.${minigame.id.path}").resolve(minigame.uuid.toString())
    }
}