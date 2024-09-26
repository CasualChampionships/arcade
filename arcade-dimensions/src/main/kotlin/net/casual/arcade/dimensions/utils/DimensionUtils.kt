package net.casual.arcade.dimensions.utils

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.mixins.level.MinecraftServerAccessor
import net.casual.arcade.utils.ArcadeUtils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.DimensionType
import org.apache.commons.io.FileUtils
import org.apache.commons.io.file.PathUtils
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.isDirectory

public fun MinecraftServer.addCustomLevel(level: CustomLevel): ServerLevel {
    val levels = (this as MinecraftServerAccessor).levels
    if (levels.containsKey(level.dimension())) {
        throw IllegalArgumentException("Tried to load level ${level.dimension().location()} when it was already loaded")
    }
    levels[level.dimension()] = level
    ServerWorldEvents.LOAD.invoker().onWorldLoad(this, level)

    if (level.persistence == LevelPersistence.Persistent) {
        LevelPersistenceTracker.mark(level.dimension())
    }
    return level
}

public fun MinecraftServer.addCustomLevel(builder: CustomLevelBuilder): ServerLevel {
    return this.addCustomLevel(builder.build(this))
}

public inline fun MinecraftServer.addCustomLevel(block: CustomLevelBuilder.() -> Unit): ServerLevel {
    val builder = CustomLevelBuilder()
    builder.block()
    return this.addCustomLevel(builder)
}

public fun MinecraftServer.loadCustomLevel(key: ResourceKey<Level>): ServerLevel? {
    val loaded = this.getLevel(key)
    if (loaded != null) {
        return loaded
    }
    val custom = CustomLevel.read(this, key) ?: return null
    return this.addCustomLevel(custom)
}

public inline fun MinecraftServer.loadOrAddCustomLevel(
    key: ResourceKey<Level>,
    block: CustomLevelBuilder.() -> Unit
): ServerLevel {
    return this.loadCustomLevel(key) ?: this.addCustomLevel { dimensionKey(key).block() }
}

public fun MinecraftServer.removeCustomLevel(level: CustomLevel, save: Boolean = true): Boolean {
    if ((this as MinecraftServerAccessor).levels.remove(level.dimension(), level)) {
        LevelPersistenceTracker.unmark(level.dimension())

        level.removePlayers()

        if (save) {
            level.save(null, flush = true, skip = false)
        }
        ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this, level)
        return true
    }
    return false
}

public fun MinecraftServer.deleteCustomLevel(level: CustomLevel): Boolean {
    if (this.removeCustomLevel(level, false)) {
        val directory = this.getDimensionPath(level.dimension())
        if (directory.isDirectory()) {
            try {
                PathUtils.deleteDirectory(directory)
            } catch (e: IOException) {
                ArcadeUtils.logger.warn("Failed to delete level directory", e)
                PathUtils.deleteOnExit(directory)
            }
        }
        return true
    }
    return false
}

public fun MinecraftServer.getDimensionPath(dimension: ResourceKey<Level>): Path {
    return (this as MinecraftServerAccessor).storage.getDimensionPath(dimension)
}

private fun ServerLevel.removePlayers() {
    val players = this.players()
    if (players.isEmpty()) {
        return
    }

    val overworld = this.server.overworld()
    for (player in players.toList()) {
        val position = player.adjustSpawnLocation(overworld, overworld.sharedSpawnPos).bottomCenter
        player.teleportTo(overworld, position.x, position.y, position.z, 0.0F, 0.0F)
    }

    if (players.isNotEmpty()) {
        ArcadeUtils.logger.warn("Failed to remove players from closing world, kicking players")
        for (player in players.toList()) {
            player.connection.disconnect(Component.literal("Failed to remove you before world closed"))
        }
    }
}