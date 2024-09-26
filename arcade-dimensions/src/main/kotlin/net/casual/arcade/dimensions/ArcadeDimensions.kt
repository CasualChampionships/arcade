package net.casual.arcade.dimensions

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.utils.*
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.dimensions.level.vanilla.extension.DragonDataExtension
import net.casual.arcade.utils.ResourceUtils
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

/**
 * Mod initializer for arcade's dimensions api.
 *
 * Also contains methods for adding, loading,
 * removing, and deleting custom worlds, which
 * can be called from Java.
 *
 * Kotlin extension functions are also provided.
 */
public object ArcadeDimensions: ModInitializer {
    override fun onInitialize() {
        DimensionRegistries.load()
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, ResourceUtils.arcade("void"), VoidChunkGenerator.CODEC)

        LevelPersistenceTracker.registerEvents()
        DragonDataExtension.registerEvents()
    }

    @JvmStatic
    public fun add(server: MinecraftServer, level: CustomLevel): ServerLevel {
        return server.addCustomLevel(level)
    }

    @JvmStatic
    public fun add(server: MinecraftServer, builder: CustomLevelBuilder): ServerLevel {
        return server.addCustomLevel(builder)
    }

    @JvmStatic
    public fun add(server: MinecraftServer, block: CustomLevelBuilder.() -> Unit): ServerLevel {
        return server.addCustomLevel(block)
    }

    @JvmStatic
    public fun load(server: MinecraftServer, key: ResourceKey<Level>): ServerLevel? {
        return server.loadCustomLevel(key)
    }

    @JvmStatic
    public fun loadOrAdd(server: MinecraftServer, key: ResourceKey<Level>, block: CustomLevelBuilder.() -> Unit): ServerLevel {
        return server.loadOrAddCustomLevel(key, block)
    }

    @JvmStatic
    @JvmOverloads
    public fun remove(server: MinecraftServer, level: CustomLevel, save: Boolean = false): Boolean {
        return server.removeCustomLevel(level, save)
    }

    @JvmStatic
    public fun delete(server: MinecraftServer, level: CustomLevel): Boolean {
        return server.deleteCustomLevel(level)
    }
}