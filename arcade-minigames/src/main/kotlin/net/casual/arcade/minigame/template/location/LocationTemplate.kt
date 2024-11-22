package net.casual.arcade.minigame.template.location

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.casual.arcade.utils.impl.Location
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import java.util.function.Function

public interface LocationTemplate {
    public fun get(level: ServerLevel): Location

    public fun codec(): MapCodec<out LocationTemplate>

    public companion object {
        public val DEFAULT: LocationTemplate = ExactLocationTemplate()

        public val CODEC: Codec<LocationTemplate> = Codec.lazyInitialized {
            MinigameRegistries.LOCATION_TEMPLATE.byNameCodec()
                .dispatch(LocationTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out LocationTemplate>>) {
            ExactLocationTemplate.register(registry)
            RandomLocationTemplate.register(registry)
            AroundLocationTemplate.register(registry)
        }
    }
}