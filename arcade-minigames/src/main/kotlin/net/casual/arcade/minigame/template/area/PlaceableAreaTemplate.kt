package net.casual.arcade.minigame.template.area

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.area.PlaceableArea
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import java.util.function.Function

public interface PlaceableAreaTemplate {
    public fun create(level: ServerLevel): PlaceableArea

    public fun codec(): MapCodec<out PlaceableAreaTemplate>

    public companion object {
        public val DEFAULT: BoxedAreaTemplate = BoxedAreaTemplate()

        public val CODEC: Codec<PlaceableAreaTemplate> = Codec.lazyInitialized {
            MinigameRegistries.PLACEABLE_AREA_TEMPLATE.byNameCodec()
                .dispatch(PlaceableAreaTemplate::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out PlaceableAreaTemplate>>) {
            StructuredAreaTemplate.register(registry)
            BoxedAreaTemplate.register(registry)
        }
    }
}