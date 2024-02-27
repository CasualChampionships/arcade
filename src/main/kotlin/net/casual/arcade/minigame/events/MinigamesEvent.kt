package net.casual.arcade.minigame.events

import com.google.gson.JsonObject
import com.mojang.datafixers.util.Either
import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.LobbyMoveToNextMinigameEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.LobbyMinigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.uuid
import net.casual.arcade.utils.MinigameUtils.transferTo
import net.casual.arcade.utils.ResourcePackUtils.hasBeenSentPack
import net.casual.arcade.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import xyz.nucleoid.fantasy.util.VoidChunkGenerator

public open class MinigamesEvent(
    public var config: MinigamesEventConfig
) {
    public lateinit var current: Minigame<*>
    private var index: Int = 0

    public fun returnToLobby(server: MinecraftServer) {
        if (this::current.isInitialized && this.current is LobbyMinigame) {
            return
        }

        val lobbyConfig = this.config.lobby
        val either: Either<RuntimeWorldHandle, ServerLevel> = if (lobbyConfig.dimension == null) {
            Either.left(createTemporaryLobbyLevel(server))
        } else {
            val level = server.getLevel(lobbyConfig.dimension)
            if (level == null) Either.left(createTemporaryLobbyLevel(server)) else Either.right(level)
        }
        val level = either.map({ it.asWorld() }, { it })
        val lobby = this.createLobbyMinigame(server, this.config.lobby.create(level))
        either.ifLeft(lobby.levels::add)

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

    public fun addPlayer(player: ServerPlayer) {
        if (!this::current.isInitialized) {
            Arcade.logger.warn("Tried adding player before minigames had started... Returning to lobby!")
            this.returnToLobby(player.server)
        }

        val current = this.current
        current.addPlayer(player)
        if (this.config.operators.contains(player.scoreboardName)) {
            current.makeAdmin(player)
        }
        this.sendResourcesTo(player, false)
    }

    public fun sendResourcesTo(player: ServerPlayer, minigame: Boolean = true) {
        for (pack in this.getAdditionalPacks().concat(this.config.packs)) {
            val info = this.getPackInfo(pack)
            if (info == null) {
                Arcade.logger.warn("MinigamesEvent tried to send resources $pack which is not available")
                continue
            }
            player.sendResourcePack(info)
        }
        if (minigame) {
            this.current.getResources().sendTo(player)
        }
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

    public open fun deserialize(json: JsonObject, server: MinecraftServer) {
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

    public open fun serialize(): JsonObject {
        val json = JsonObject()
        JsonObject().also { minigame ->
            minigame["id"] = this.current.id.toString()
            minigame["uuid"] = this.current.uuid.toString()
            minigame["index"] = this.index
            json["current_minigame"] = minigame
        }
        return json
    }

    protected open fun getPackInfo(name: String): PackInfo? {
        return null
    }

    protected open fun getAdditionalPacks(): List<String> {
        return listOf()
    }

    protected open fun createLobbyMinigame(server: MinecraftServer, lobby: Lobby): LobbyMinigame {
        return LobbyMinigame(server, lobby)
    }

    protected open fun createTemporaryLobbyLevel(server: MinecraftServer): RuntimeWorldHandle {
        val config = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(VoidChunkGenerator(server))
        return Fantasy.get(server).openTemporaryWorld(config)
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
}