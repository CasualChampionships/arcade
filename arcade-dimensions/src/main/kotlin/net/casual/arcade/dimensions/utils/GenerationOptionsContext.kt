package net.casual.arcade.dimensions.utils

import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.biome.BiomeManager
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
public object GenerationOptionsContext {
    private var current: LevelGenerationOptions? = null

    @Internal
    @JvmStatic
    public fun get(server: MinecraftServer): LevelGenerationOptions {
        if (!server.isSameThread) {
            throw IllegalStateException("Tried getting generation context off-thread")
        }
        return this.current ?: throw IllegalStateException("Generation context was not set")
    }

    @Internal
    internal fun set(server: MinecraftServer, options: LevelGenerationOptions): Long {
        if (!server.isSameThread) {
            throw IllegalStateException("Tried setting generation context off-thread")
        }
        if (this.current != null) {
            throw IllegalStateException("Tried setting generation context within another generation context")
        }
        this.current = options
        return BiomeManager.obfuscateSeed(options.seed)
    }

    @Internal
    internal fun reset(server: MinecraftServer) {
        if (!server.isSameThread) {
            throw IllegalStateException("Tried resetting generation context off-thread")
        }
        this.current = null
    }
}