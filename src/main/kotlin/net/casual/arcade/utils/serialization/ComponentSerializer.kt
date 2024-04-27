package net.casual.arcade.utils.serialization

import com.google.gson.JsonParseException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.serializer
import me.senseiwells.nametag.CustomNameTags
import net.minecraft.network.chat.Component

@OptIn(ExperimentalSerializationApi::class)
public object ComponentSerializer: KSerializer<Component> {
    override val descriptor: SerialDescriptor = SerialDescriptor("Component", serializer<JsonElement>().descriptor)

    override fun deserialize(decoder: Decoder): Component {
        if (decoder !is JsonDecoder) {
            throw IllegalArgumentException("Can only deserialize components as JSON")
        }
        val componentJson = decoder.json.encodeToString(decoder.decodeJsonElement())
        try {
            val component = Component.Serializer.fromJson(componentJson)
            if (component != null) {
                return component
            }
        } catch (_: JsonParseException) {

        }
        CustomNameTags.logger.error("Failed to deserialize text $componentJson")
        return Component.empty()
    }

    override fun serialize(encoder: Encoder, value: Component) {
        if (encoder !is JsonEncoder) {
            throw IllegalArgumentException("Can only serialize components as JSON")
        }
        encoder.encodeJsonElement(
            encoder.json.parseToJsonElement(Component.Serializer.toJson(value))
        )
    }
}