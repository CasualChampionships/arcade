/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.spawner.extension.LevelCustomMobSpawningExtension
import net.casual.arcade.dimensions.level.vanilla.extension.DragonDataExtension
import net.casual.arcade.dimensions.utils.*
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.utils.ArcadeUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
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
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, ArcadeUtils.id("void"), VoidChunkGenerator.CODEC)

        LevelPersistenceTracker.registerEvents()
        LevelCustomMobSpawningExtension.registerEvents()
        DragonDataExtension.registerEvents()
    }

    /**
     * Adds a [CustomLevel] to the server.
     *
     * This registers the level to the server and
     * will fire the [ServerWorldEvents.LOAD] fabric event.
     *
     * Once added, the level will be fully functional and be
     * ticked and can accept players.
     *
     * This method may be called if the [level] instance has
     * already been registered, it will simply just return the
     * instance. If there is a different level instance
     * already added with the same dimension key, then this
     * method will throw an exception.
     *
     * @param level The level to add.
     * @return The added level.
     */
    @JvmStatic
    public fun add(server: MinecraftServer, level: CustomLevel): CustomLevel {
        return server.addCustomLevel(level)
    }

    /**
     * Adds a [CustomLevel] to the server.
     *
     * This registers the level to the server and
     * will fire the [ServerWorldEvents.LOAD] fabric event.
     *
     * Once added, the level will be fully functional and be
     * ticked and can accept players.
     *
     * This will throw if there is a level already added
     * with the same dimension key.
     *
     * @param server The server to add the level to.
     * @param builder The builder to create the level.
     * @return The added level.
     */
    @JvmStatic
    public fun add(server: MinecraftServer, builder: CustomLevelBuilder): CustomLevel {
        return server.addCustomLevel(builder)
    }

    /**
     * Adds a [CustomLevel] to the server.
     *
     * This registers the level to the server and
     * will fire the [ServerWorldEvents.LOAD] fabric event.
     *
     * Once added, the level will be fully functional and be
     * ticked and can accept players.
     *
     * This will throw if there is a level already added
     * with the same dimension key.
     *
     * @param server The server to add the level to.
     * @param block The method to configure the builder.
     * @return The added level.
     */
    @JvmStatic
    public fun add(server: MinecraftServer, block: CustomLevelBuilder.() -> Unit): CustomLevel {
        return server.addCustomLevel(block)
    }

    /**
     * Loads a [CustomLevel] from the server.
     *
     * This will try and read the level's dimension data
     * and then re-construct the [CustomLevel].
     * Once the level is re-constructed it is added to the
     * [MinecraftServer].
     *
     * If there is no dimension data or if reading/parsing
     * the dimension data fails this method will return `null`.
     *
     * @param server The server to load the level from.
     * @param location The location of the level.
     * @return The level, or `null` if it does not exist.
     */
    @JvmStatic
    public fun load(server: MinecraftServer, location: ResourceLocation): CustomLevel? {
        return server.loadCustomLevel(location)
    }

    /**
     * Loads a [CustomLevel] from the server.
     *
     * This will try and read the level's dimension data
     * and then re-construct the [CustomLevel].
     * Once the level is re-constructed it is added to the
     * [MinecraftServer].
     *
     * If there is no dimension data or if reading/parsing
     * the dimension data fails this method will return `null`.
     *
     * @param server The server to load the level from.
     * @param key The key of the level.
     * @return The level, or `null` if it does not exist.
     */
    @JvmStatic
    public fun load(server: MinecraftServer, key: ResourceKey<Level>): CustomLevel? {
        return server.loadCustomLevel(key)
    }

    /**
     * Loads a [CustomLevel] from the server.
     *
     * This will try and read the level's dimension data
     * and then re-construct the [CustomLevel].
     * Once the level is re-constructed it is added to the
     * [MinecraftServer].
     *
     * If there is no dimension data or if reading/parsing
     * the dimension data fails, this method will add the level
     * instead, using the provided builder method.
     *
     * @param server The server to load the level from.
     * @param location The location of the level.
     * @param block The method to configure the builder.
     * @return The level.
     */
    @JvmStatic
    public fun loadOrAdd(server: MinecraftServer, location: ResourceLocation, block: CustomLevelBuilder.() -> Unit): CustomLevel {
        return server.loadOrAddCustomLevel(location, block)
    }

    /**
     * Loads a [CustomLevel] from the server.
     *
     * This will try and read the level's dimension data
     * and then re-construct the [CustomLevel].
     * Once the level is re-constructed it is added to the
     * [MinecraftServer].
     *
     * If there is no dimension data or if reading/parsing
     * the dimension data fails, this method will add the level
     * instead, using the provided builder method.
     *
     * @param server The server to load the level from.
     * @param key The key of the level.
     * @param block The method to configure the builder.
     * @return The level.
     */
    @JvmStatic
    public fun loadOrAdd(server: MinecraftServer, key: ResourceKey<Level>, block: CustomLevelBuilder.() -> Unit): CustomLevel {
        return server.loadOrAddCustomLevel(key, block)
    }

    /**
     * Returns whether the server has the exact [level] instance.
     *
     * This checks the [level] reference *not* the dimension key.
     * It is possible for the server to have a different level
     * instance under the same dimension key, in which case
     * this method will return `false`.
     *
     * If you want to check whether a level with a specific
     * dimension key is loaded, you can do:
     * ```
     * server.levelKeys().contains(dimensionKey)
     * ```
     *
     * @param level The level to check for.
     * @return Whether the server has the specified level.
     */
    @JvmStatic
    public fun hasCustomLevel(server: MinecraftServer, level: CustomLevel): Boolean {
        return server.hasCustomLevel(level)
    }

    /**
     * Removes a [CustomLevel] from the server.
     *
     * This will remove the level from the server and
     * will fire the [ServerWorldEvents.UNLOAD] fabric event.
     *
     * If the [CustomLevel.persistence] is [LevelPersistence.Temporary]
     * then the level will be deleted instead, equivalent of calling
     * [deleteCustomLevel].
     *
     * Players should be removed from the level before calling.
     * Any remaining players in the level will be removed and
     * teleported to the overworld, if that fails, then players will
     * be kicked from the server.
     *
     * @param level The level to remove.
     * @return `true` if the level was removed, `false` otherwise.
     */
    @JvmStatic
    public fun remove(server: MinecraftServer, level: CustomLevel): Boolean {
        return server.removeCustomLevel(level)
    }

    /**
     * Deletes a [CustomLevel] from the server.
     *
     * This will delete **both** permanent and temporary worlds if
     * you want to remove permanent worlds but delete temporary worlds
     * you want to call [removeCustomLevel] instead.
     *
     * The level's directory will be deleted after it is removed.
     *
     * @param level The level to delete.
     * @return `true` if the level was deleted, `false` otherwise.
     * @see removeCustomLevel
     */
    @JvmStatic
    public fun delete(server: MinecraftServer, level: CustomLevel): Boolean {
        return server.deleteCustomLevel(level)
    }
}