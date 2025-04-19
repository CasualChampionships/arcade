package net.casual.arcade.utils

import com.google.gson.JsonParseException
import com.google.gson.Strictness
import com.google.gson.internal.Streams
import com.google.gson.stream.JsonReader
import com.mojang.brigadier.StringReader
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.minecraft.core.HolderLookup

/**
 * Mostly based on [net.minecraft.commands.ParserUtils]
 * in previous versions to parse JSON.
 */
public object ParserUtils {
    private val JSON_READER_POS by lazy {
        val field = JsonReader::class.java.getDeclaredField("pos")
        field.isAccessible = true
        field
    }
    private val JSON_READER_LINESTART by lazy {
        val field = JsonReader::class.java.getDeclaredField("lineStart")
        field.isAccessible = true
        field
    }

    public fun JsonReader.getPos(): Int {
        return JSON_READER_POS.getInt(this) - JSON_READER_LINESTART.getInt(this)
    }

    public fun <T> parseJson(registries: HolderLookup.Provider, reader: StringReader, codec: Codec<T>): T {
        val jsonReader = JsonReader(java.io.StringReader(reader.remaining))
        jsonReader.strictness = Strictness.STRICT

        try {
            return codec.parse(
                registries.createSerializationContext(JsonOps.INSTANCE),
                Streams.parse(jsonReader)
            ).getOrThrow(::JsonParseException)
        } catch (e: StackOverflowError) {
            throw JsonParseException(e)
        } finally {
            reader.cursor += jsonReader.getPos()
        }
    }
}