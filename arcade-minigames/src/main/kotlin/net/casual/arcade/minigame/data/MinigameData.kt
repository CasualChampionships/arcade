/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data

import com.mojang.serialization.Codec
import net.casual.arcade.minigame.data.impl.MinigameWorldData
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.file.ReadableArchive
import net.minecraft.core.Registry
import net.minecraft.server.MinecraftServer

public interface MinigameData {
    public fun type(): MinigameDataType<*>

    public interface Provider<D: MinigameData> {
        public val type: MinigameDataType<D>

        public fun get(archive: ReadableArchive, server: MinecraftServer): D

        public companion object {
            public val CODEC: Codec<Provider<*>> = Codec.lazyInitialized {
                MinigameRegistries.MINIGAME_DATA_PROVIDER.byNameCodec()
            }

            internal fun bootstrap(registry: Registry<Provider<*>>) {
                MinigameWorldData.register(registry)
            }

            public fun Provider<*>.register(registry: Registry<Provider<*>>) {
                Registry.register(registry, this.type.id, this)
            }
        }
    }
}
