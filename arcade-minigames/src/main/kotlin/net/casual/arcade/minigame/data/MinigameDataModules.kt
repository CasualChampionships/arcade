/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.minecraft.server.MinecraftServer

public interface MinigameDataModules: AutoCloseable {
    public val id: String

    public fun <M: MinigameDataModule> get(type: Class<M>): M?

    public fun <M: MinigameDataModule> has(vararg types: Class<out M>): Boolean

    override fun close()

    private object EmptyMinigameDataModules: MinigameDataModules {
        override val id: String
            get() = "empty"

        override fun <M : MinigameDataModule> get(type: Class<M>): M? {
            return null
        }

        override fun <M : MinigameDataModule> has(vararg types: Class<out M>): Boolean {
            return false
        }

        override fun close() {

        }
    }

    private class ArchivedMinigameDataModules(
        private val archive: ReadableArchive
    ): MinigameDataModules {
        val modules = Reference2ObjectOpenHashMap<Class<*>, MinigameDataModule>()

        override val id: String
            get() = this.archive.name

        @Suppress("UNCHECKED_CAST")
        override fun <M: MinigameDataModule> get(type: Class<M>): M? {
            return this.modules[type] as? M
        }

        override fun <M: MinigameDataModule> has(vararg types: Class<out M>): Boolean {
            return types.all { this.modules.containsKey(it) }
        }

        override fun close() {
            this.archive.close()
        }
    }

    private class OverridingMinigameDataModules(
        private val parent: MinigameDataModules,
        private val overrides: Reference2ObjectOpenHashMap<Class<*>, MinigameDataModule>
    ): MinigameDataModules {
        override val id: String
            get() = this.parent.id

        @Suppress("UNCHECKED_CAST")
        override fun <M : MinigameDataModule> get(type: Class<M>): M? {
            return this.overrides[type] as? M ?: this.parent.get(type)
        }

        override fun <M : MinigameDataModule> has(vararg types: Class<out M>): Boolean {
            return types.all { this.overrides.containsKey(it) || this.parent.has(it) }
        }

        override fun close() {
            this.parent.close()
        }
    }

    public companion object {
        public fun empty(): MinigameDataModules {
            return EmptyMinigameDataModules
        }

        public fun from(archive: ReadableArchive, server: MinecraftServer): MinigameDataModules {
            val codec = MinigameDataModule.Provider.CODEC.listOf()
            val providers = archive.parseJson("minigame_data_modules.json", codec).getOrNull()
                ?: throw IllegalArgumentException("Archive ${archive.name} doesn't have a minigame_data_modules.json")
            val data = ArchivedMinigameDataModules(archive)
            for (provider in providers) {
                val module = provider.get(archive, server)
                data.modules[module::class.java] = module
            }
            return data
        }

        public inline fun <reified M: MinigameDataModule> MinigameDataModules.get(): M? {
            return this.get(M::class.java)
        }

        public fun MinigameDataModules.with(vararg modules: MinigameDataModule): MinigameDataModules {
            val map = Reference2ObjectOpenHashMap<Class<*>, MinigameDataModule>(modules.size)
            modules.associateByTo(map) { it::class.java }
            return OverridingMinigameDataModules(this, map)
        }
    }
}