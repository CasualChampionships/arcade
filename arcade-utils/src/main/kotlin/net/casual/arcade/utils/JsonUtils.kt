/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
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
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isReadable
import kotlin.io.path.notExists
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlin.io.path.writer
import kotlin.jvm.optionals.getOrNull

@Suppress("DEPRECATION")
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

    @Deprecated("Use decodeRaw instead", ReplaceWith("this.decodeRaw(reader)"))
    public inline fun <reified T: Any> decode(reader: Reader): T {
        return this.decodeRaw(reader)
    }

    @Deprecated("Use decodeWith(..., reader) instead", ReplaceWith("this.decodeWith(decoder, stream.reader())"))
    public fun <T: Any> decodeWith(decoder: Decoder<T>, stream: InputStream): DataResult<T> {
        return this.decodeWith(decoder, stream.reader())
    }

    @Deprecated("Use decodeRaw instead", ReplaceWith("this.decodeRaw(reader)"))
    public fun decodeToJsonElement(reader: Reader): JsonElement {
        return this.decodeRaw(reader)
    }

    @Deprecated("Use decodeRaw instead", ReplaceWith("this.decodeRaw(reader)"))
    public fun decodeToJsonArray(reader: Reader): JsonArray {
        return this.decodeRaw(reader)
    }

    @Deprecated("Use decodeRaw instead", ReplaceWith("this.decodeRaw(reader)"))
    public fun decodeToJsonObject(reader: Reader): JsonObject {
        return this.decodeRaw(reader)
    }

    @Deprecated("Use decodeRaw instead", ReplaceWith("this.decodeRaw(string.reader())"))
    public fun decodeToJsonObject(string: String): JsonObject {
        return this.decodeRaw(string.reader())
    }

    @Deprecated("Use encodeWith(any, encoder, writer) instead")
    public fun <T> encodeWith(encoder: Encoder<T>, any: T, writer: Appendable): Boolean {
        val result = encoder.encodeStart(JsonOps.INSTANCE, any).result()
        if (result.isPresent) {
            this.encodeRaw(result.get(), writer)
            return true
        }
        return false
    }

    @Deprecated("Replace with encodeRaw", ReplaceWith("this.encodeRaw(json, writer)"))
    public fun encode(json: JsonElement, writer: Appendable) {
        this.encodeRaw(json, writer)
    }

    @Deprecated("Replace with encodeRaw", ReplaceWith("this.encodeRaw(any, writer)"))
    public fun encode(any: Any, writer: Appendable) {
        this.encodeRaw(any, writer)
    }

    @Deprecated("Replace with encodeRaw", ReplaceWith("this.encodeRaw(any)"))
    public fun encodeToElement(any: Any?): JsonElement {
        return this.encodeRaw(any)
    }

    // TODO: We should start deprecating the below methods
    //   Replace with JsonValueInput/Output

    @Deprecated("For removal")
    public fun JsonObject.getWithNull(key: String): JsonElement? {
        val value = this.get(key) ?: return null
        return if (value.isJsonNull) null else value
    }

    @Deprecated("For removal")
    public fun <T> Collection<T>.toJsonObject(serializer: (T) -> Pair<String, JsonElement>): JsonObject {
        val json = JsonObject()
        for (element in this) {
            val (key, value) = serializer(element)
            json.add(key, value)
        }
        return json
    }

    @Deprecated("For removal")
    public fun Collection<JsonElement>.toJsonArray(): JsonArray {
        val array = JsonArray(this.size)
        for (element in this) {
            array.add(element)
        }
        return array
    }

    @Deprecated("For removal")
    public fun <T> Collection<T>.toJsonArray(serializer: (T) -> JsonElement): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    @Deprecated("For removal")
    public fun <T> Collection<T>.toJsonBooleanArray(serializer: (T) -> Boolean): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    @Deprecated("For removal")
    public fun <T> Collection<T>.toJsonStringArray(serializer: (T) -> String): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    @Deprecated("For removal")
    public fun <T> Collection<T>.toJsonNumberArray(serializer: (T) -> Number): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    @Deprecated("For removal")
    public fun JsonObject.hasNonNull(key: String): Boolean {
        return this.has(key) && !this.get(key).isJsonNull
    }

    @Deprecated("For removal")
    public fun JsonObject.boolean(key: String): Boolean {
        return this.get(key).asBoolean
    }

    @Deprecated("For removal")
    public fun JsonObject.booleanOrNull(key: String): Boolean? {
        return this.getWithNull(key)?.asBoolean
    }

    @Deprecated("For removal")
    public fun JsonObject.booleanOrDefault(key: String, default: Boolean = false): Boolean {
        return this.getWithNull(key)?.asBoolean ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.booleanOrPut(key: String, putter: () -> Boolean = { false }): Boolean {
        return this.booleanOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    @Deprecated("For removal")
    public fun JsonObject.string(key: String): String {
        return this.get(key).asString
    }

    @Deprecated("For removal")
    public fun JsonObject.stringOrNull(key: String): String? {
        return this.getWithNull(key)?.asString
    }

    @Deprecated("For removal")
    public fun JsonObject.stringOrDefault(key: String, default: String = ""): String {
        return this.getWithNull(key)?.asString ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.stringOrPut(key: String, putter: () -> String = { "" }): String {
        return this.stringOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    @Deprecated("For removal")
    public fun JsonObject.uuid(key: String): UUID {
        return UUID.fromString(this.string(key))
    }

    @Deprecated("For removal")
    public fun JsonObject.uuidOrNull(key: String): UUID? {
        return UUID.fromString(this.getWithNull(key)?.asString ?: return null)
    }

    @Deprecated("For removal")
    public fun JsonObject.uuidOrDefault(key: String, default: UUID = Util.NIL_UUID): UUID {
        return UUID.fromString(this.getWithNull(key)?.asString ?: return default)
    }

    @Deprecated("For removal")
    public fun JsonObject.uuidOrPut(key: String, putter: () -> UUID = { Util.NIL_UUID }): UUID {
        return this.uuidOrNull(key) ?: putter().also { this.addProperty(key, it.toString()) }
    }

    @Deprecated("For removal")
    public fun JsonObject.number(key: String): Number {
        return this.get(key).asNumber
    }

    @Deprecated("For removal")
    public fun JsonObject.numberOrNull(key: String): Number? {
        return this.getWithNull(key)?.asNumber
    }

    @Deprecated("For removal")
    public fun JsonObject.numberOrDefault(key: String, default: Number = 0): Number {
        return this.getWithNull(key)?.asNumber ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.numberOrPut(key: String, putter: () -> Number = { 0 }): Number {
        return this.numberOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    @Deprecated("For removal")
    public fun JsonObject.int(key: String): Int {
        return this.get(key).asInt
    }

    @Deprecated("For removal")
    public fun JsonObject.intOrNull(key: String): Int? {
        return this.getWithNull(key)?.asInt
    }

    @Deprecated("For removal")
    public fun JsonObject.intOrDefault(key: String, default: Int = 0): Int {
        return this.getWithNull(key)?.asInt ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.intOrPut(key: String, putter: () -> Int = { 0 }): Int {
        return this.intOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    @Deprecated("For removal")
    public fun JsonObject.long(key: String): Long {
        return this.get(key).asLong
    }

    @Deprecated("For removal")
    public fun JsonObject.longOrNull(key: String): Long? {
        return this.getWithNull(key)?.asLong
    }

    @Deprecated("For removal")
    public fun JsonObject.longOrDefault(key: String, default: Long = 0L): Long {
        return this.getWithNull(key)?.asLong ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.longOrPut(key: String, putter: () -> Long = { 0L }): Long {
        return this.longOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    @Deprecated("For removal")
    public fun JsonObject.float(key: String): Float {
        return this.get(key).asFloat
    }

    @Deprecated("For removal")
    public fun JsonObject.floatOrNull(key: String): Float? {
        return this.getWithNull(key)?.asFloat
    }

    @Deprecated("For removal")
    public fun JsonObject.floatOrDefault(key: String, default: Float = 0.0F): Float {
        return this.getWithNull(key)?.asFloat ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.floatOrPut(key: String, putter: () -> Float = { 0.0F }): Float {
        return this.floatOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    @Deprecated("For removal")
    public fun JsonObject.double(key: String): Double {
        return this.get(key).asDouble
    }

    @Deprecated("For removal")
    public fun JsonObject.doubleOrNull(key: String): Double? {
        return this.getWithNull(key)?.asDouble
    }

    @Deprecated("For removal")
    public fun JsonObject.doubleOrDefault(key: String, default: Double = 0.0): Double {
        return this.getWithNull(key)?.asDouble ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.doubleOrPut(key: String, putter: () -> Double = { 0.0 }): Double {
        return this.doubleOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    @Deprecated("For removal")
    public fun JsonObject.array(key: String): JsonArray {
        return this.get(key).asJsonArray
    }

    @Deprecated("For removal")
    public fun JsonObject.arrayOrNull(key: String): JsonArray? {
        return this.getWithNull(key)?.asJsonArray
    }

    @Deprecated("For removal")
    public fun JsonObject.arrayOrDefault(key: String, default: JsonArray = JsonArray()): JsonArray {
        return this.getWithNull(key)?.asJsonArray ?: default
    }

    @Deprecated("For removal")
    public fun JsonObject.obj(key: String): JsonObject {
        return this.get(key).asJsonObject
    }

    @Deprecated("For removal")
    public fun JsonObject.objOrNull(key: String): JsonObject? {
        return this.getWithNull(key)?.asJsonObject
    }

    @Deprecated("For removal")
    public fun JsonObject.objOrDefault(key: String, default: JsonObject = JsonObject()): JsonObject {
        return this.getWithNull(key)?.asJsonObject ?: default
    }

    @Deprecated("For removal")
    public operator fun JsonObject.set(key: String, value: String?) {
        this.addProperty(key, value)
    }

    @Deprecated("For removal")
    public operator fun JsonObject.set(key: String, value: Number?) {
        this.addProperty(key, value)
    }

    @Deprecated("For removal")
    public operator fun JsonObject.set(key: String, value: Boolean?) {
        this.addProperty(key, value)
    }

    @Deprecated("For removal")
    public operator fun JsonObject.set(key: String, value: JsonElement?) {
        this.add(key, value)
    }

    @Deprecated("For removal")
    public fun JsonArray.booleans(): Iterable<Boolean> {
        return this.map(JsonElement::getAsBoolean)
    }

    public fun JsonArray.strings(): Iterable<String> {
        return this.map(JsonElement::getAsString)
    }

    @Deprecated("For removal")
    public fun JsonArray.uuids(): Iterable<UUID> {
        return this.map { element -> UUID.fromString(element.asString) }
    }

    @Deprecated("For removal")
    public fun JsonArray.ints(): Iterable<Int> {
        return this.map(JsonElement::getAsInt)
    }

    @Deprecated("For removal")
    public fun JsonArray.floats(): Iterable<Float> {
        return this.map(JsonElement::getAsFloat)
    }

    @Deprecated("For removal")
    public fun JsonArray.doubles(): Iterable<Double> {
        return this.map(JsonElement::getAsDouble)
    }

    @Deprecated("For removal")
    public fun JsonArray.arrays(): Iterable<JsonArray> {
        return this.map(JsonElement::getAsJsonArray)
    }

    @Deprecated("For removal")
    public fun JsonArray.objects(): Iterable<JsonObject> {
        return this.map(JsonElement::getAsJsonObject)
    }

    @Deprecated("For removal")
    public fun <T> JsonArray.map(mapper: JsonElement.() -> T): Iterable<T> {
        return object: Iterable<T> {
            override fun iterator(): Iterator<T> {
                return object: Iterator<T> {
                    private var index = 0

                    override fun hasNext(): Boolean {
                        return this.index < size()
                    }

                    override fun next(): T {
                        return mapper(get(this.index++))
                    }
                }
            }
        }
    }

    @Deprecated("For removal")
    public fun JsonObject.toCompoundTag(): CompoundTag {
        val tag = CompoundTag()
        for ((key, value) in this.asMap()) {
            tag.put(key, value.toTag())
        }
        return tag
    }

    @Deprecated("For removal")
    public fun JsonArray.toListTag(): ListTag {
        val tag = ListTag()
        for (element in this) {
            tag.add(element.toTag())
        }
        return tag
    }

    @Deprecated("For removal")
    public fun JsonPrimitive.toTag(): Tag {
        return when {
            this.isBoolean -> if (this.asBoolean) ByteTag.ONE else ByteTag.ZERO
            this.isNumber -> DoubleTag.valueOf(this.asDouble)
            else -> return StringTag.valueOf(this.asString)
        }
    }

    @Deprecated("For removal")
    public fun JsonElement.toTag(): Tag {
        return when (this) {
            is JsonObject -> this.toCompoundTag()
            is JsonArray -> this.toListTag()
            is JsonPrimitive ->  this.toTag()
            else -> EndTag.INSTANCE
        }
    }

    @Deprecated("For removal")
    public fun CompoundTag.toJsonObject(): JsonObject {
        val json = JsonObject()
        for (key in this.keySet()) {
            if (this.contains(key)) {
                val value = this.get(key)!!
                json.add(key, value.toJsonElement())
            }
        }
        return json
    }

    @Deprecated("For removal")
    public fun CollectionTag.toJsonArray(): JsonArray {
        val json = JsonArray()
        for (tag in this) {
            json.add(tag.toJsonElement())
        }
        return json
    }

    @Deprecated("For removal")
    public fun Tag.toJsonElement(): JsonElement {
        return when (this) {
            is CompoundTag -> this.toJsonObject()
            is CollectionTag -> this.toJsonArray()
            is StringTag -> JsonPrimitive(this.value)
            is NumericTag -> JsonPrimitive(this.box())
            else -> JsonNull.INSTANCE
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
}