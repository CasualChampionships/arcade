/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame

import com.google.common.collect.LinkedHashMultimap
import com.mojang.serialization.Dynamic
import com.mojang.serialization.JsonOps
import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.events.server.ServerStopEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.minigame.commands.ExtendedGameModeCommand
import net.casual.arcade.minigame.commands.MinigameCommand
import net.casual.arcade.minigame.commands.PauseCommand
import net.casual.arcade.minigame.commands.TeamCommandModifier
import net.casual.arcade.minigame.compat.MinigamesReplayCompat
import net.casual.arcade.minigame.exception.MinigameCreationException
import net.casual.arcade.minigame.exception.MinigameSerializationException
import net.casual.arcade.minigame.extensions.PlayerMinigameExtension
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension
import net.casual.arcade.minigame.gamemode.ExtendedGameMode
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameSerializer
import net.casual.arcade.minigame.serialization.SerializableMinigame
import net.casual.arcade.minigame.serialization.save
import net.casual.arcade.minigame.managers.phase.AdvancingPhaseRoutine
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.minigame.utils.MinigameUtils
import net.casual.arcade.scheduler.utils.TaskRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.storage.LevelResource
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
    private val minigamesByUUID = LinkedHashMap<UUID, Minigame>()
    private val minigamesById = LinkedHashMultimap.create<Identifier, Minigame>()

    /**
     * This method gets all the current running minigames.
     *
     * @return All the current running minigames.
     */
    public fun all(): Collection<Minigame> {
        return Collections.unmodifiableCollection(this.minigamesByUUID.values)
    }

    /**
     * This gets the minigame that is associated with the given [UUID].
     *
     * @param uuid The uuid of the minigame.
     * @return The minigame with the given uuid.
     */
    public fun get(uuid: UUID): Minigame? {
        return this.minigamesByUUID[uuid]
    }

    /**
     * This gets all the minigames that are associated with a given [Identifier].
     *
     * @param id The id of the minigame.
     * @return All the minigames with the given id.
     */
    public fun get(id: Identifier): List<Minigame> {
        return this.minigamesById.get(id).toList()
    }

    public fun create(
        id: Identifier,
        server: MinecraftServer,
        data: Dynamic<*> = Dynamic(JsonOps.INSTANCE)
    ): Minigame {
        val codec = MinigameRegistries.MINIGAME_FACTORY.getOptional(id).getOrNull()
            ?: throw MinigameCreationException("Cannot create Minigame $id, no such factory found")
        val factory = codec.codec().parse(data).getOrThrow {
            MinigameCreationException("Failed to create Minigame $id with default factory parameters")
        }
        try {
            return factory.create(MinigameCreationContext(server))
        } catch (e: Exception) {
            throw MinigameCreationException("Failed to create Minigame $id", e)
        }
    }

    public fun read(path: Path, server: MinecraftServer): Minigame {
        return MinigameSerializer.createFrom(path, server)
    }

    override fun onInitialize() {
        MinigameRegistries.load()
        MinigamesReplayCompat.registerEvents()
        MinigameUtils.registerEvents()
        ExtendedGameMode.registerEvents()
        PlayerMovementRestrictionExtension.registerEvents()
        PlayerMinigameExtension.registerEvents()

        GlobalEventHandler.Server.register<ServerStartEvent> { (server) ->
            this.loadMinigames(server)
        }
        GlobalEventHandler.Server.register<ServerSaveEvent> {
            this.saveMinigames()
        }
        GlobalEventHandler.Server.register<ServerStopEvent> {
            this.closeMinigames()
        }
        GlobalEventHandler.Server.register<ServerStopEvent>(phase = BuiltInEventPhases.POST) {
            this.awaitMinigames()
        }
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent> { event ->
            event.register(ExtendedGameModeCommand, MinigameCommand, PauseCommand, TeamCommandModifier)
        }

        AdvancingPhaseRoutine.register(TaskRegistries.ROUTINE)
    }

    internal fun allById(): Map<Identifier, Collection<Minigame>> {
        return this.minigamesById.asMap()
    }

    internal fun register(minigame: Minigame) {
        this.minigamesByUUID[minigame.uuid] = minigame
        this.minigamesById.put(minigame.id, minigame)
    }

    internal fun unregister(minigame: Minigame) {
        this.minigamesByUUID.remove(minigame.uuid)
        this.minigamesById[minigame.id].remove(minigame)

        minigame.serializer.submit {
            val path = minigame.getSavePath()
            if (path.exists()) {
                @OptIn(ExperimentalPathApi::class)
                path.deleteRecursively()
            }
        }
    }

    internal fun getInstancesSavePath(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("minigames").resolve("instances")
    }

    internal fun getPath(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("minigames")
    }

    private fun loadMinigames(server: MinecraftServer) {
        val path = this.getInstancesSavePath(server)
        path.createDirectories()
        for (types in path.listDirectoryEntries()) {
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
                    ArcadeUtils.logger.error("Failed to create minigame", e)
                    this.quarantine(server, types.fileName.toString(), minigame)
                }
            }
        }
    }

    private fun quarantine(server: MinecraftServer, type: String, path: Path) {
        val directory = this.getPath(server).resolve("quarantined").resolve(type)
        try {
            directory.createDirectories()

            var destination = directory.resolve(path.fileName.toString())
            var index = 1
            while (destination.exists()) {
                destination = directory.resolve("${path.fileName}-${index++}")
            }
            path.moveTo(destination)
            ArcadeUtils.logger.error("Moved unloadable minigame data from $path to $destination")
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to quarantine unloadable minigame data at $path", e)
        }
    }

    private fun saveMinigames() {
        for (minigame in this.minigamesByUUID.values) {
            if (minigame is SerializableMinigame) {
                try {
                    minigame.save()
                } catch (e: MinigameSerializationException) {
                    ArcadeUtils.logger.error("Failed to write minigame", e)
                }
            }
        }
    }

    private fun closeMinigames() {
        for (minigame in ArrayList(this.minigamesByUUID.values)) {
            if (minigame !is SerializableMinigame) {
                minigame.close()
            }
        }
    }

    private fun awaitMinigames() {
        MinigameSerializer.awaitPending()
    }
}