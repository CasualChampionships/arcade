package net.casual.arcade.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

public object JsonUtils {
    private fun JsonObject.getWithNull(key: String): JsonElement? {
        val value = this.get(key) ?: return null
        return if (value.isJsonNull) null else value
    }

    public fun <T> Collection<T>.toJsonObject(serializer: (T) -> Pair<String, JsonElement>): JsonObject {
        val json = JsonObject()
        for (element in this) {
            val (key, value) = serializer(element)
            json.add(key, value)
        }
        return json
    }

    public fun <T> Collection<T>.toJsonArray(serializer: (T) -> JsonElement): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    public fun <T> Collection<T>.toJsonBooleanArray(serializer: (T) -> Boolean): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    public fun <T> Collection<T>.toJsonStringArray(serializer: (T) -> String): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    public fun <T> Collection<T>.toJsonNumberArray(serializer: (T) -> Number): JsonArray {
        return this.stream().map { serializer(it) }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
    }

    public fun JsonObject.hasNonNull(key: String): Boolean {
        return this.has(key) && !this.get(key).isJsonNull
    }

    public fun JsonObject.boolean(key: String): Boolean {
        return this.get(key).asBoolean
    }

    public fun JsonObject.booleanOrNull(key: String): Boolean? {
        return this.getWithNull(key)?.asBoolean
    }

    public fun JsonObject.booleanOrDefault(key: String, default: Boolean = false): Boolean {
        return this.getWithNull(key)?.asBoolean ?: default
    }

    public fun JsonObject.booleanOrPut(key: String, putter: () -> Boolean = { false }): Boolean {
        return this.booleanOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    public fun JsonObject.string(key: String): String {
        return this.get(key).asString
    }

    public fun JsonObject.stringOrNull(key: String): String? {
        return this.getWithNull(key)?.asString
    }

    public fun JsonObject.stringOrDefault(key: String, default: String = ""): String {
        return this.getWithNull(key)?.asString ?: default
    }

    public fun JsonObject.stringOrPut(key: String, putter: () -> String = { "" }): String {
        return this.stringOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    public fun JsonObject.number(key: String): Number {
        return this.get(key).asNumber
    }

    public fun JsonObject.numberOrNull(key: String): Number? {
        return this.getWithNull(key)?.asNumber
    }

    public fun JsonObject.numberOrDefault(key: String, default: Number = 0): Number {
        return this.getWithNull(key)?.asNumber ?: default
    }

    public fun JsonObject.numberOrPut(key: String, putter: () -> Number = { 0 }): Number {
        return this.numberOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    public fun JsonObject.int(key: String): Int {
        return this.get(key).asInt
    }

    public fun JsonObject.intOrNull(key: String): Int? {
        return this.getWithNull(key)?.asInt
    }

    public fun JsonObject.intOrDefault(key: String, default: Int = 0): Int {
        return this.getWithNull(key)?.asInt ?: default
    }

    public fun JsonObject.intOrPut(key: String, putter: () -> Int = { 0 }): Int {
        return this.intOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    public fun JsonObject.float(key: String): Float {
        return this.get(key).asFloat
    }

    public fun JsonObject.floatOrNull(key: String): Float? {
        return this.getWithNull(key)?.asFloat
    }

    public fun JsonObject.floatOrDefault(key: String, default: Float = 0.0F): Float {
        return this.getWithNull(key)?.asFloat ?: default
    }

    public fun JsonObject.floatOrPut(key: String, putter: () -> Float = { 0.0F }): Float {
        return this.floatOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    public fun JsonObject.double(key: String): Double {
        return this.get(key).asDouble
    }

    public fun JsonObject.doubleOrNull(key: String): Double? {
        return this.getWithNull(key)?.asDouble
    }

    public fun JsonObject.doubleOrDefault(key: String, default: Double = 0.0): Double {
        return this.getWithNull(key)?.asDouble ?: default
    }

    public fun JsonObject.doubleOrPut(key: String, putter: () -> Double = { 0.0 }): Double {
        return this.doubleOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    public fun JsonObject.array(key: String): JsonArray {
        return this.get(key).asJsonArray
    }

    public fun JsonObject.arrayOrNull(key: String): JsonArray? {
        return this.getWithNull(key)?.asJsonArray
    }

    public fun JsonObject.arrayOrDefault(key: String, default: JsonArray = JsonArray()): JsonArray {
        return this.getWithNull(key)?.asJsonArray ?: default
    }

    public fun JsonObject.arrayOrPut(key: String, putter: () -> JsonArray = ::JsonArray): JsonArray {
        return this.arrayOrNull(key) ?: putter().also { this.add(key, it) }
    }

    public fun JsonObject.obj(key: String): JsonObject {
        return this.get(key).asJsonObject
    }

    public fun JsonObject.objOrNull(key: String): JsonObject? {
        return this.getWithNull(key)?.asJsonObject
    }

    public fun JsonObject.objOrDefault(key: String, default: JsonObject = JsonObject()): JsonObject {
        return this.getWithNull(key)?.asJsonObject ?: default
    }

    public fun JsonObject.objOrPut(key: String, putter: () -> JsonObject = ::JsonObject): JsonObject {
        return this.objOrNull(key) ?: putter().also { this.add(key, it) }
    }

    public fun JsonArray.booleans(): Iterable<Boolean> {
        return this.map(JsonElement::getAsBoolean)
    }

    public fun JsonArray.strings(): Iterable<String> {
        return this.map(JsonElement::getAsString)
    }

    public fun JsonArray.ints(): Iterable<Int> {
        return this.map(JsonElement::getAsInt)
    }

    public fun JsonArray.floats(): Iterable<Float> {
        return this.map(JsonElement::getAsFloat)
    }

    public fun JsonArray.doubles(): Iterable<Double> {
        return this.map(JsonElement::getAsDouble)
    }

    public fun JsonArray.arrays(): Iterable<JsonArray> {
        return this.map(JsonElement::getAsJsonArray)
    }

    public fun JsonArray.objects(): Iterable<JsonObject> {
        return this.map(JsonElement::getAsJsonObject)
    }

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
}