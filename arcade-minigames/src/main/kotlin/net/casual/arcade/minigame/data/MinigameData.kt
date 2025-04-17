package net.casual.arcade.minigame.data

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.minecraft.server.MinecraftServer

public class MinigameData private constructor(
    private val archive: ReadableArchive
): AutoCloseable {
    private val modules = Reference2ObjectOpenHashMap<Class<*>, MinigameDataModule>()

    public val id: String
        get() = this.archive.name

    @Suppress("UNCHECKED_CAST")
    public fun <M: MinigameDataModule> get(type: Class<M>): M? {
        return this.modules[type] as? M
    }

    public inline fun <reified M: MinigameDataModule> get(): M? {
        return this.get(M::class.java)
    }

    public fun <M: MinigameDataModule> has(vararg types: Class<out M>): Boolean {
        return types.all { this.modules.containsKey(it) }
    }

    override fun close() {
        this.archive.close()
    }

    public companion object {
        public fun from(archive: ReadableArchive, server: MinecraftServer): MinigameData {
            val codec = MinigameDataModule.Provider.CODEC.listOf()
            val providers = archive.parseJson("minigame_data_modules.json", codec).getOrNull()
                ?: throw IllegalArgumentException("Archive ${archive.name} doesn't have a minigame_data_modules.json")
            val data = MinigameData(archive)
            for (provider in providers) {
                val module = provider.get(archive, server)
                data.modules[module::class.java] = module
            }
            return data
        }
    }
}