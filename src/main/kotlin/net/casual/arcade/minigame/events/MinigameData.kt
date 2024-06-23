package net.casual.arcade.minigame.events

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.serialization.ArcadeExtraCodecs
import net.minecraft.resources.ResourceLocation
import java.util.*

public data class MinigameData(
    val id: ResourceLocation,
    val customData: Optional<JsonObject>
) {
    public companion object {
        public val CODEC: Codec<MinigameData> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(MinigameData::id),
                ArcadeExtraCodecs.JSON_OBJECT.optionalFieldOf("custom_data").forGetter(MinigameData::customData)
            ).apply(instance, ::MinigameData)
        }
    }
}