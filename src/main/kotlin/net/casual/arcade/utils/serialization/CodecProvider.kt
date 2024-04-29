package net.casual.arcade.utils.serialization

import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

@Suppress("PropertyName")
public interface CodecProvider<T> {
    public val ID: ResourceLocation

    public val CODEC: Codec<out T>

    public companion object {
        public fun <T> CodecProvider<out T>.register(registry: Registry<Codec<out T>>) {
            Registry.register(registry, this.ID, this.CODEC)
        }
    }
}