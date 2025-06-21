/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.utils

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.Util
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.LevelResource
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*

@Internal
public object LevelPersistenceTracker {
    private val CODEC = ArcadeExtraCodecs.DIMENSION.listOf()

    private val persistent = ReferenceLinkedOpenHashSet<ResourceKey<Level>>()
    private val temporary = ReferenceLinkedOpenHashSet<ResourceKey<Level>>()

    private val deletion = ObjectOpenHashSet<Path>()

    @Volatile private var dirty = false

    internal fun markAsPersistent(key: ResourceKey<Level>) {
        this.persistent.add(key)
    }

    internal fun unmarkAsPersistent(key: ResourceKey<Level>) {
        this.persistent.remove(key)
    }

    internal fun markAsTemporary(server: MinecraftServer, key: ResourceKey<Level>) {
        this.deletion.add(server.getDimensionPath(key))
        this.temporary.add(key)
        this.dirty = true
    }

    @JvmStatic
    public fun loadPersistentLevels(server: MinecraftServer): Collection<ResourceKey<Level>> {
        this.persistent.addAll(this.readLevelKeysFrom(this.getTemporaryDataPath(server)))
        return this.persistent
    }

    private fun cleanupTemporaryLevels(server: MinecraftServer) {
        this.temporary.addAll(this.readLevelKeysFrom(this.getTemporaryDataPath(server)))
        val iter = this.temporary.iterator()
        for (key in iter) {
            if (this.tryDeleteTemporaryDimension(server, key)) {
                iter.remove()
            }
        }
        this.writeLevelKeysTo(this.getTemporaryDataPath(server), this.temporary.toList())
    }

    private fun readLevelKeysFrom(path: Path): ArrayList<ResourceKey<Level>> {
        val keys = ArrayList<ResourceKey<Level>>()
        if (path.isRegularFile()) {
            try {
                val tag = NbtIo.read(path)?.get("level_keys")
                for (key in CODEC.parse(NbtOps.INSTANCE, tag).orThrow) {
                    keys.add(key)
                }
            } catch (e: IllegalStateException) {
                ArcadeUtils.logger.error("Failed to parse level keys", e)
            } catch (e: IOException) {
                ArcadeUtils.logger.error("Failed to parser level keys", e)
            }
        }
        return keys
    }

    private fun writeLevelKeysTo(
        path: Path,
        keys: List<ResourceKey<Level>>
    ) {
        CompletableFuture.runAsync({
            try {
                val compound = CompoundTag()
                compound.put("level_keys", CODEC.encodeStart(NbtOps.INSTANCE, keys).orThrow)
                NbtIo.write(compound, path.createParentDirectories())
            } catch (e: IllegalStateException) {
                ArcadeUtils.logger.error("Failed to encode level keys", e)
            } catch (e: IOException) {
                ArcadeUtils.logger.error("Failed to encode level keys", e)
            }
        }, Util.ioPool())
    }

    @OptIn(ExperimentalPathApi::class)
    private fun tryDeleteTemporaryDimension(server: MinecraftServer, dimension: ResourceKey<Level>): Boolean {
        val path = server.getDimensionPath(dimension)
        if (path.notExists()) {
            return true
        }
        try {
            path.deleteRecursively()
            return true
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to cleanup temporary dimension ${dimension.location()}", e)
            return false
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerSaveEvent> { (server) ->
            this.writeLevelKeysTo(this.getPersistenceDataPath(server), this.persistent.toList())
        }
        GlobalEventHandler.Server.register<ServerLoadedEvent> { (server) ->
            this.cleanupTemporaryLevels(server)
        }
        GlobalEventHandler.Server.register<ServerTickEvent> { (server) ->
            if (this.dirty) {
                this.writeLevelKeysTo(this.getTemporaryDataPath(server), this.temporary.toList())
                this.dirty = false
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            for (path in this.deletion) {
                try {
                    if (path.isDirectory()) {
                        @OptIn(ExperimentalPathApi::class)
                        path.deleteRecursively()
                    }
                } catch (e: Exception) {
                    ArcadeUtils.logger.error("Failed to remove temporary level files", e)
                }
            }
        })
    }

    private fun getPersistenceDataPath(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("arcade").resolve("persistent-levels.nbt")
    }

    private fun getTemporaryDataPath(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("arcade").resolve("temporary-levels.nbt")
    }
}