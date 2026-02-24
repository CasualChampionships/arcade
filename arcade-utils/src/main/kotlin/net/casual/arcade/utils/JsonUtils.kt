/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import com.google.gson.*
import com.google.gson.internal.Streams
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.mojang.brigadier.StringReader
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import com.mojang.serialization.Encoder
import com.mojang.serialization.JsonOps
import net.casual.arcade.utils.serialization.createSerializationContext
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.*
import net.minecraft.util.Util
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.lang.reflect.Field
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isReadable
import kotlin.io.path.notExists
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlin.io.path.writer
import kotlin.jvm.optionals.getOrNull

public object JsonUtils {
    public val GSON: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create()
    public val MIN_GSON: Gson = GsonBuilder().serializeNulls().disableHtmlEscaping().create()

    public inline fun <reified T: Any> decodeRaw(element: JsonElement): T {
        return GSON.fromJson(element, object: TypeToken<T>() { })
    }

    public inline fun <reified T: Any> decodeRaw(reader: Reader): T {
        return GSON.fromJson(reader, object: TypeToken<T>() { })
    }

    public inline fun <reified T: Any> decodeRaw(path: Path): T {
        return path.reader().use(this::decodeRaw)
    }

    public fun <T: Any> decodeWith(
        decoder: Decoder<T>,
        reader: Reader,
        lookup: HolderLookup.Provider? = null
    ): DataResult<T> {
        return decoder.parse(lookup.createSerializationContext(JsonOps.INSTANCE), decodeRaw(reader))
    }

    public fun <T: Any> decodeWithOr(
        decoder: Decoder<T>,
        reader: Reader,
        lookup: HolderLookup.Provider? = null,
        getter: () -> T
    ): T {
        return this.decodeWith(decoder, reader, lookup).resultOrPartial().orElseGet(getter)
    }

    public fun <T: Any> decodeWith(
        decoder: Decoder<T>,
        path: Path,
        lookup: HolderLookup.Provider? = null
    ): DataResult<T> {
        return this.getReaderSafely(path).flatMap { reader ->
            reader.use { this.decodeWith(decoder, it, lookup) }
        }
    }

    public fun <T: Any> decodeWithOr(
        decoder: Decoder<T>,
        path: Path,
        lookup: HolderLookup.Provider? = null,
        getter: () -> T
    ): T {
        val reader = this.getReaderSafely(path).result().getOrNull() ?: return getter.invoke()
        return reader.use { this.decodeWithOr(decoder, it, lookup, getter) }
    }

    public fun encodeRaw(any: Any?): JsonElement {
        return GSON.toJsonTree(any)
    }

    public fun encodeRaw(any: Any?, appendable: Appendable) {
        GSON.toJson(any, appendable)
    }

    public fun encodeRaw(any: Any?, path: Path) {
        path.writer().use { this.encodeRaw(any, it) }
    }

    public fun <T: Any> encodeWith(
        any: T,
        encoder: Encoder<T>,
        lookup: HolderLookup.Provider? = null
    ): DataResult<JsonElement> {
        return encoder.encodeStart(lookup.createSerializationContext(JsonOps.INSTANCE), any)
    }

    public fun <T: Any> encodeWith(
        any: T,
        encoder: Encoder<T>,
        appendable: Appendable,
        lookup: HolderLookup.Provider? = null
    ) {
        val encoded = this.encodeWith(any, encoder, lookup).getOrThrow(::IllegalArgumentException)
        GSON.toJson(encoded, appendable)
    }

    public fun <T: Any> encodeWith(
        any: T,
        encoder: Encoder<T>,
        path: Path,
        lookup: HolderLookup.Provider? = null
    ) {
        path.writer().use { writer ->
            this.encodeWith(any, encoder, writer, lookup)
        }
    }

    public fun <T: Any> encodeWith(
        any: T,
        encoder: Encoder<T>,
        path: Path,
        lookup: HolderLookup.Provider? = null,
        formatter: (String) -> String
    ) {
        val builder = StringBuilder()
        this.encodeWith(any, encoder, builder, lookup)
        path.writeText(formatter.invoke(builder.toString()))
    }


    public fun JsonReader.getPos(): Int {
        return JsonReaderFields.pos.getInt(this) - JsonReaderFields.lineStart.getInt(this)
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

    private fun getReaderSafely(path: Path): DataResult<InputStreamReader> {
        if (path.notExists()) {
            return DataResult.error { "Path $path doesn't exist" }
        }
        if (!path.isReadable()) {
            return DataResult.error { "Path $path is not readable" }
        }
        try {
            return DataResult.success(path.reader())
        } catch (exception: IOException) {
            val message = exception.message
            return DataResult.error { "Failed to open reader at $path: $message" }
        }
    }

    private object JsonReaderFields {
        val pos: Field by lazy {
            val field = JsonReader::class.java.getDeclaredField("pos")
            field.isAccessible = true
            field
        }
        val lineStart: Field by lazy {
            val field = JsonReader::class.java.getDeclaredField("lineStart")
            field.isAccessible = true
            field
        }
    }
}