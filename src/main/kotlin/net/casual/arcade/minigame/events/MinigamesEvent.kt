package net.casual.arcade.minigame.events

import com.google.gson.JsonObject
import com.mojang.datafixers.util.Either
import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.LobbyMoveToNextMinigameEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.uuid
import net.casual.arcade.utils.MinigameUtils.transferTo
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import xyz.nucleoid.fantasy.util.VoidChunkGenerator

public class MinigamesEvent(
    public var config: MinigamesEventConfig,
    private val lobbyFactory: (MinecraftServer, Lobby) -> LobbyMinigame = ::LobbyMinigame
) {
    public lateinit var current: Minigame<*>
    private var index: Int = 0

    public fun returnToLobby(server: MinecraftServer) {
        val lobbyConfig = this.config.lobby
        val either: Either<RuntimeWorldHandle, ServerLevel> = if (lobbyConfig.dimension == null) {
            Either.left(createTemporaryLobbyLevel(server))
        } else {
            val level = server.getLevel(lobbyConfig.dimension)
            if (level == null) Either.left(createTemporaryLobbyLevel(server)) else Either.right(level)
        }
        val level = either.map({ it.asWorld() }, { it })
        val lobby = this.lobbyFactory(server, this.config.lobby.create(level))
        either.ifLeft(lobby::addLevel)

        lobby.events.register<LobbyMoveToNextMinigameEvent> {
            this.incrementIndex(it.next)
            this.current = it.next
        }

        this.startNewMinigame(lobby)

        val next = this.createNextMinigame(server)
        if (next != null) {
            lobby.setNextMinigame(next)
        }
    }

    public fun startNewMinigame(minigame: Minigame<*>) {
        this.incrementIndex(minigame)
        if (this::current.isInitialized) {
            this.current.transferTo(minigame)
        } else {
            minigame.start()
        }
        this.current = minigame
    }

    public fun getNextMinigameId(): ResourceLocation? {
        val minigames = this.config.minigames

        if (this.index !in minigames.indices) {
            if (this.config.repeat) {
                this.index = 0
                if (this.config.minigames.isNotEmpty()) {
                    return this.config.minigames[0]
                }
            }
            return null
        }
        return this.config.minigames[this.index]
    }

    public fun getMinigameIndex(): Int {
        return this.index
    }

    public fun setMinigameIndex(index: Int) {
        this.index = index
    }

    public fun getMinigames(): List<ResourceLocation> {
        return this.config.minigames
    }

    public fun reloadConfig(config: MinigamesEventConfig) {
        this.config = config
    }

    public fun deserialize(json: JsonObject, server: MinecraftServer) {
        val current = json.obj("current_minigame")
        val id = ResourceLocation(current.string("id"))
        val index = current.int("index")

        this.index = index
        if (id == LobbyMinigame.ID) {
            this.returnToLobby(server)
            return
        }

        val uuid = current.uuid("uuid")
        val minigame = Minigames.get(uuid)
        if (minigame == null) {
            this.returnToLobby(server)
            return
        }
        this.current = minigame
    }

    public fun serialize(): JsonObject {
        val json = JsonObject()
        JsonObject().also { minigame ->
            minigame["id"] = this.current.id.toString()
            minigame["uuid"] = this.current.uuid.toString()
            minigame["index"] = this.index
            json["current_minigame"] = minigame
        }
        return json
    }

    private fun createNextMinigame(server: MinecraftServer): Minigame<*>? {
        val minigameId = this.getNextMinigameId() ?: return null
        val factory = Minigames.getFactory(minigameId)
        if (factory == null) {
            Arcade.logger.error("Failed to create next minigame, non-existent factory")
            return null
        }
        return factory.create(MinigameCreationContext(server))
    }

    private fun incrementIndex(next: Minigame<*>) {
        val nextId = this.getNextMinigameId()
        if (nextId != null && next.id == nextId) {
            this.index++
        }
    }

    private fun createTemporaryLobbyLevel(server: MinecraftServer): RuntimeWorldHandle {
        val config = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(VoidChunkGenerator(server))
        return Fantasy.get(server).openTemporaryWorld(config)
    }
}