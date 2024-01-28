package net.casual.arcade.minigame

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

/**
 * This object is used for registering and holding
 * all the current minigames that are running.
 */
public object Minigames {
    private val GSON = GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create()
    private val PATH = Arcade.path.resolve("minigames.json")

    private val ALL = LinkedHashMap<UUID, Minigame<*>>()
    private val BY_ID = LinkedHashMap<ResourceLocation, ArrayList<Minigame<*>>>()
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
        return Collections.unmodifiableList(this.BY_ID[id] ?: return emptyList())
    }

    @Internal
    public fun byLevel(level: ServerLevel): Minigame<*>? {
        for (minigame in ALL.values) {
            if (minigame.hasLevel(level)) {
                return minigame
            }
        }
        return null
    }

    internal fun allById(): Map<ResourceLocation, ArrayList<Minigame<*>>> {
        return this.BY_ID
    }

    internal fun register(minigame: Minigame<*>) {
        this.ALL[minigame.uuid] = minigame
        this.BY_ID.getOrPut(minigame.id) { ArrayList() }.add(minigame)
    }

    internal fun unregister(minigame: Minigame<*>) {
        this.ALL.remove(minigame.uuid)
        this.BY_ID[minigame.id]?.removeIf { it.uuid == minigame.uuid }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerLoadedEvent> { this.readMinigames(it.server) }
        GlobalEventHandler.register<ServerSaveEvent> { this.saveMinigames() }
    }

    private fun readMinigames(server: MinecraftServer) {
        if (!PATH.exists()) {
            return
        }
        val games = try {
            PATH.bufferedReader().use { GSON.fromJson(it, JsonArray::class.java).objects() }
        } catch (e: Exception) {
            Arcade.logger.error("Failed to read minigames from json!", e)
            return
        }
        for (game in games) {
            val id = ResourceLocation(game.string("id"))
            val factory = FACTORIES_BY_ID[id]
            if (factory == null) {
                Arcade.logger.warn("Failed to reload minigame with id $id, no factory found!")
                continue
            }

            val data = game.obj("data")
            val context = MinigameCreationContext(server, data.objOrNull("custom"))
            try {
                val minigame = factory.create(context)
                register(minigame)
                if (minigame is SavableMinigame) {
                    minigame.read(data)
                } else {
                    Arcade.logger.warn("Minigame with id $id loaded but was not Savable?!?")
                }
            } catch (e: Exception) {
                Arcade.logger.error("Failed to reload minigame (${id}) on restart", e)
            }
        }
    }

    private fun saveMinigames() {
        val games = JsonArray()
        for (minigame in ALL.values) {
            if (minigame is SavableMinigame) {
                val game = JsonObject()
                game.addProperty("id", minigame.id.toString())
                game.addProperty("uuid", minigame.uuid.toString())
                game.add("data", minigame.save())
                games.add(game)
            }
        }

        try {
            PATH.bufferedWriter().use { GSON.toJson(games, it) }
        } catch (e: Exception) {
            Arcade.logger.error("Failed to write minigames as json!\n\n${GSON.toJson(games)}", e)
        }
    }
}