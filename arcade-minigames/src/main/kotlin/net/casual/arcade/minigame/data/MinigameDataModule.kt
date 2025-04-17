package net.casual.arcade.minigame.data

import com.mojang.serialization.Codec
import net.casual.arcade.minigame.data.module.MinigameWorldData
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.file.ReadableArchive
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer

public interface MinigameDataModule {
    public interface Provider {
        public val id: ResourceLocation

        public fun get(archive: ReadableArchive, server: MinecraftServer): MinigameDataModule

        public companion object {
            public val CODEC: Codec<Provider> = Codec.lazyInitialized {
                MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER.byNameCodec()
            }

            internal fun bootstrap(registry: Registry<Provider>) {
                MinigameWorldData.register(registry)
            }

            public fun Provider.register(registry: Registry<Provider>) {
                Registry.register(registry, this.id, this)
            }
        }
    }
}