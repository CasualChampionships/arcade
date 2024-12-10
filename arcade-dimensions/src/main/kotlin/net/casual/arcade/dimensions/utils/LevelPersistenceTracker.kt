package net.casual.arcade.dimensions.utils

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
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
import kotlin.io.path.isRegularFile

@Internal
public object LevelPersistenceTracker {
    private val CODEC = ArcadeExtraCodecs.DIMENSION.listOf()

    private val persistent = ReferenceLinkedOpenHashSet<ResourceKey<Level>>()

    internal fun mark(key: ResourceKey<Level>) {
        this.persistent.add(key)
    }

    internal fun unmark(key: ResourceKey<Level>) {
        this.persistent.remove(key)
    }

    @JvmStatic
    public fun load(server: MinecraftServer): Collection<ResourceKey<Level>> {
        val path = this.getPersistenceDataPath(server)
        if (path.isRegularFile()) {
            try {
                val keys = NbtIo.read(path)?.get("level_keys")
                for (key in CODEC.parse(NbtOps.INSTANCE, keys).orThrow) {
                    this.persistent.add(key)
                }
            } catch (e: IllegalStateException) {
                ArcadeUtils.logger.error("Failed to parse persistent level keys", e)
            } catch (e: IOException) {
                ArcadeUtils.logger.error("Failed to read persistent level keys", e)
            }
        }
        return this.persistent
    }

    private fun save(server: MinecraftServer) {
        val path = this.getPersistenceDataPath(server)
        try {
            val compound = CompoundTag()
            compound.put("level_keys", CODEC.encodeStart(NbtOps.INSTANCE, this.persistent.toList()).orThrow)
            NbtIo.write(compound, path)
        } catch (e: IllegalStateException) {
            ArcadeUtils.logger.error("Failed to encode persistent level keys", e)
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to write persistent level keys", e)
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerSaveEvent> { (server) ->
            this.save(server)
        }
    }

    private fun getPersistenceDataPath(server: MinecraftServer): Path {
        return server.getWorldPath(LevelResource.ROOT).resolve("arcade-persistent-levels.nbt")
    }
}