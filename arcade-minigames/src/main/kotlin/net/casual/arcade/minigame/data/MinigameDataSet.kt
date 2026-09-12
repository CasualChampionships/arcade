/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.minigame.component.MinigameComponent
import net.casual.arcade.minigame.component.MinigameComponentType
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.minecraft.server.MinecraftServer

/**
 * An immutable collection of [MinigameData].
 *
 * This collection can be indexed by [MinigameDataType] to
 * get each respective data module.
 *
 * Typically, data sets are loaded in from disk via the
 * [from] function with a [ReadableArchive], but can also
 * be dynamically generated with [from] and with [with].
 *
 * @see MinigameData
 */
public interface MinigameDataSet: AutoCloseable {
    /**
     * The identifier of the dataset.
     *
     * This isn't required to be unique, and typically
     * reflects the name of the archive on disk.
     */
    public val id: String

    /**
     * This gets the [MinigameData] by providing its [MinigameDataType].
     * Will return `null` if the data doesn't exist.
     *
     * @param type The type of the data to get.
     * @return The stored data, may be `null` if not added.
     */
    public fun <D: MinigameData> get(type: MinigameDataType<D>): D?

    /**
     * This gets the [MinigameData] by providing its [MinigameDataType].
     * Will throw if the component doesn't exist.
     *
     * @param type The type of the data to get.
     * @return The stored data.
     * @throws IllegalArgumentException If the data is missing.
     */
    public fun <D: MinigameData> require(type: MinigameDataType<D>): D {
        return requireNotNull(this.get(type)) { "Minigame data ${this.id} has no data for $type" }
    }

    /**
     * Checks whether all data with the given [types] exists.
     *
     * Typically, this should be called after the set has been
     * created to verify that the set contains all the data
     * that is required by your minigame. Subsequent calls
     * to [require] with the [types] are guaranteed to be safe.
     *
     * @param types The types to check.
     * @return Whether all data with those types exist.
     */
    public fun has(vararg types: MinigameDataType<*>): Boolean

    /**
     * Closes the set and cleans up after it.
     */
    override fun close()

    private object EmptyMinigameDataSet: MinigameDataSet {
        override val id: String
            get() = "empty"

        override fun <D: MinigameData> get(type: MinigameDataType<D>): D? {
            return null
        }

        override fun has(vararg types: MinigameDataType<*>): Boolean {
            return false
        }

        override fun close() {

        }
    }

    private class ArchivedMinigameDataSet(
        private val archive: ReadableArchive
    ): MinigameDataSet {
        val data = Object2ObjectOpenHashMap<MinigameDataType<*>, MinigameData>()

        override val id: String
            get() = this.archive.name

        @Suppress("UNCHECKED_CAST")
        override fun <D: MinigameData> get(type: MinigameDataType<D>): D? {
            return this.data[type] as? D
        }

        override fun has(vararg types: MinigameDataType<*>): Boolean {
            return types.all { this.data.containsKey(it) }
        }

        override fun close() {
            this.archive.close()
        }
    }

    private class OverridingMinigameDataSet(
        private val parent: MinigameDataSet,
        private val overrides: Object2ObjectOpenHashMap<MinigameDataType<*>, MinigameData>
    ): MinigameDataSet {
        override val id: String
            get() = this.parent.id

        @Suppress("UNCHECKED_CAST")
        override fun <D: MinigameData> get(type: MinigameDataType<D>): D? {
            return this.overrides[type] as? D ?: this.parent.get(type)
        }

        override fun has(vararg types: MinigameDataType<*>): Boolean {
            return types.all { this.overrides.containsKey(it) || this.parent.has(it) }
        }

        override fun close() {
            this.parent.close()
        }
    }

    private class CustomMinigameDataSet(
        override val id: String,
        private val data: Object2ObjectOpenHashMap<MinigameDataType<*>, MinigameData>
    ): MinigameDataSet {
        @Suppress("UNCHECKED_CAST")
        override fun <D : MinigameData> get(type: MinigameDataType<D>): D? {
            return this.data[type] as? D
        }

        override fun has(vararg types: MinigameDataType<*>): Boolean {
            return types.all { this.data.containsKey(it) }
        }

        override fun close() {

        }
    }

    public companion object {
        private const val DATA_FILE = "minigame_data.json"

        /**
         * The empty data set, this has no data modules.
         *
         * @return The empty [MinigameDataSet].
         */
        public fun empty(): MinigameDataSet {
            return EmptyMinigameDataSet
        }

        /**
         * Creates a data set from the given [archive].
         *
         * The [archive] provided must have a `minigame_data.json` file
         * which lists all the [MinigameDataType]s that the [archive]
         * provides and will use this to fetch [MinigameDataProvider]s
         * to create the [MinigameData] instances.
         *
         * @param archive The archive to create the data set from.
         * @param server The server context.
         * @return The created data set.
         * @throws IllegalArgumentException If the archive doesn't have a `minigame_data.json`.
         */
        public fun from(archive: ReadableArchive, server: MinecraftServer): MinigameDataSet {
            val codec = MinigameDataProvider.CODEC.listOf()
            val providers = archive.parseJson(DATA_FILE, codec).getOrNull()
                ?: throw IllegalArgumentException("Archive ${archive.name} doesn't have a $DATA_FILE")
            val set = ArchivedMinigameDataSet(archive)
            for (provider in providers) {
                val data = provider.get(archive, server)
                check(data.type() == provider.type) { "MinigameDataProvider ${provider.type} created ${data.type()}!?" }
                set.data[provider.type] = data
            }
            return set
        }

        /**
         * Creates a dynamic data set from the provided [id] and [MinigameData]s.
         *
         * @param id The id of the set.
         * @param data The data that makes up the set.
         * @return The created data set.
         */
        public fun from(id: String, vararg data: MinigameData): MinigameDataSet {
            val map = Object2ObjectOpenHashMap<MinigameDataType<*>, MinigameData>(data.size)
            data.associateByTo(map, MinigameData::type)
            return CustomMinigameDataSet(id, map)
        }

        /**
         * Extends an existing data set with the provided [data].
         *
         * @param data The additional data.
         * @return An extended data set.
         */
        public fun MinigameDataSet.with(vararg data: MinigameData): MinigameDataSet {
            val map = Object2ObjectOpenHashMap<MinigameDataType<*>, MinigameData>(data.size)
            data.associateByTo(map, MinigameData::type)
            return OverridingMinigameDataSet(this, map)
        }
    }
}
