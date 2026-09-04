package net.casual.arcade.minigame.data

import com.mojang.serialization.Codec
import net.casual.arcade.minigame.data.impl.MinigameWorldData
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.file.ReadableArchive
import net.minecraft.core.Registry
import net.minecraft.server.MinecraftServer

/**
 * This provides a specific implementation of [MinigameData]
 * for a given [ReadableArchive].
 *
 * Your [MinigameDataProvider] instances must be registered
 * to the [MinigameRegistries.MINIGAME_DATA_PROVIDER] registry
 * to allow [MinigameData] to be resolved automatically.
 *
 * @see MinigameData
 */
public interface MinigameDataProvider<D: MinigameData> {
    /**
     * The type of the [MinigameData] this is providing.
     */
    public val type: MinigameDataType<D>

    /**
     * Gets the minigame data with the specified [type] from
     * the given [archive].
     *
     * @param archive The archive containing the data.
     * @param server The server that is loading this data.
     * @return The [MinigameData] instance.
     */
    public fun get(archive: ReadableArchive, server: MinecraftServer): D

    public companion object {
        public val CODEC: Codec<MinigameDataProvider<*>> = Codec.lazyInitialized {
            MinigameRegistries.MINIGAME_DATA_PROVIDER.byNameCodec()
        }

        internal fun bootstrap(registry: Registry<MinigameDataProvider<*>>) {
            MinigameWorldData.register(registry)
        }

        public fun MinigameDataProvider<*>.register(registry: Registry<MinigameDataProvider<*>>) {
            Registry.register(registry, this.type.id, this)
        }
    }
}