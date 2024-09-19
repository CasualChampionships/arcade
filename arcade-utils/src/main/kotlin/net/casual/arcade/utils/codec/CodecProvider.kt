package net.casual.arcade.utils.codec

import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

@Suppress("PropertyName")
public interface CodecProvider<T> {
    public val ID: ResourceLocation

    public val CODEC: MapCodec<out T>

    public companion object {
        public fun <T> CodecProvider<out T>.register(registry: Registry<MapCodec<out T>>) {
            Registry.register(registry, this.ID, this.CODEC)
        }
    }
}