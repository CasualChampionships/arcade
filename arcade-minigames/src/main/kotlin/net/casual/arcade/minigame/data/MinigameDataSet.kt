/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.minecraft.server.MinecraftServer

public interface MinigameDataSet: AutoCloseable {
    public val id: String

    public fun <D: MinigameData> get(type: MinigameDataType<D>): D?

    public fun <D: MinigameData> require(type: MinigameDataType<D>): D {
        return requireNotNull(this.get(type)) { "Minigame data ${this.id} has no data for $type" }
    }

    public fun has(vararg types: MinigameDataType<*>): Boolean

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

    public companion object {
        private const val DATA_FILE = "minigame_data.json"

        public fun empty(): MinigameDataSet {
            return EmptyMinigameDataSet
        }

        public fun from(archive: ReadableArchive, server: MinecraftServer): MinigameDataSet {
            val codec = MinigameData.Provider.CODEC.listOf()
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

        public fun MinigameDataSet.with(vararg data: MinigameData): MinigameDataSet {
            val map = Object2ObjectOpenHashMap<MinigameDataType<*>, MinigameData>(data.size)
            data.associateByTo(map, MinigameData::type)
            return OverridingMinigameDataSet(this, map)
        }
    }
}
