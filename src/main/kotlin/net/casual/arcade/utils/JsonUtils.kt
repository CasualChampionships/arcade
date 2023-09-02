package net.casual.arcade.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object JsonUtils {
    fun JsonObject.boolean(key: String): Boolean {
        return this.get(key).asBoolean
    }

    fun JsonObject.booleanOrNull(key: String): Boolean? {
        return this.get(key)?.asBoolean
    }

    fun JsonObject.booleanOrDefault(key: String, default: Boolean = false): Boolean {
        return this.get(key)?.asBoolean ?: default
    }

    fun JsonObject.booleanOrPut(key: String, putter: () -> Boolean = { false }): Boolean {
        return this.booleanOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    fun JsonObject.string(key: String): String {
        return this.get(key).asString
    }

    fun JsonObject.stringOrNull(key: String): String? {
        return this.get(key)?.asString
    }

    fun JsonObject.stringOrDefault(key: String, default: String = ""): String {
        return this.get(key)?.asString ?: default
    }

    fun JsonObject.stringOrPut(key: String, putter: () -> String = { "" }): String {
        return this.stringOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    fun JsonObject.number(key: String): Number {
        return this.get(key).asNumber
    }

    fun JsonObject.numberOrNull(key: String): Number? {
        return this.get(key)?.asNumber
    }

    fun JsonObject.numberOrDefault(key: String, default: Number = 0): Number {
        return this.get(key)?.asNumber ?: default
    }

    fun JsonObject.numberOrPut(key: String, putter: () -> Number = { 0 }): Number {
        return this.numberOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    fun JsonObject.int(key: String): Int {
        return this.get(key).asInt
    }

    fun JsonObject.intOrNull(key: String): Int? {
        return this.get(key)?.asInt
    }

    fun JsonObject.intOrDefault(key: String, default: Int = 0): Int {
        return this.get(key)?.asInt ?: default
    }

    fun JsonObject.intOrPut(key: String, putter: () -> Int = { 0 }): Int {
        return this.intOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    fun JsonObject.float(key: String): Float {
        return this.get(key).asFloat
    }

    fun JsonObject.floatOrNull(key: String): Float? {
        return this.get(key)?.asFloat
    }

    fun JsonObject.floatOrDefault(key: String, default: Float = 0.0F): Float {
        return this.get(key)?.asFloat ?: default
    }

    fun JsonObject.floatOrPut(key: String, putter: () -> Float = { 0.0F }): Float {
        return this.floatOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    fun JsonObject.double(key: String): Double {
        return this.get(key).asDouble
    }

    fun JsonObject.doubleOrNull(key: String): Double? {
        return this.get(key)?.asDouble
    }

    fun JsonObject.doubleOrDefault(key: String, default: Double = 0.0): Double {
        return this.get(key)?.asDouble ?: default
    }

    fun JsonObject.doubleOrPut(key: String, putter: () -> Double = { 0.0 }): Double {
        return this.doubleOrNull(key) ?: putter().also { this.addProperty(key, it) }
    }

    fun JsonObject.array(key: String): JsonArray {
        return this.get(key).asJsonArray
    }

    fun JsonObject.arrayOrNull(key: String): JsonArray? {
        return this.get(key)?.asJsonArray
    }

    fun JsonObject.arrayOrDefault(key: String, default: JsonArray = JsonArray()): JsonArray {
        return this.get(key)?.asJsonArray ?: default
    }

    fun JsonObject.arrayOrPut(key: String, putter: () -> JsonArray = ::JsonArray): JsonArray {
        return this.arrayOrNull(key) ?: putter().also { this.add(key, it) }
    }

    fun JsonObject.obj(key: String): JsonObject {
        return this.get(key).asJsonObject
    }

    fun JsonObject.objOrNull(key: String): JsonObject? {
        return this.get(key)?.asJsonObject
    }

    fun JsonObject.objOrDefault(key: String, default: JsonObject = JsonObject()): JsonObject {
        return this.get(key)?.asJsonObject ?: default
    }

    fun JsonObject.objOrPut(key: String, putter: () -> JsonObject = ::JsonObject): JsonObject {
        return this.objOrNull(key) ?: putter().also { this.add(key, it) }
    }

    fun JsonArray.booleans(): Iterable<Boolean> {
        return this.mapped(JsonElement::getAsBoolean)
    }

    fun JsonArray.strings(): Iterable<String> {
        return this.mapped(JsonElement::getAsString)
    }

    fun JsonArray.ints(): Iterable<Int> {
        return this.mapped(JsonElement::getAsInt)
    }

    fun JsonArray.floats(): Iterable<Float> {
        return this.mapped(JsonElement::getAsFloat)
    }

    fun JsonArray.doubles(): Iterable<Double> {
        return this.mapped(JsonElement::getAsDouble)
    }

    fun JsonArray.arrays(): Iterable<JsonArray> {
        return this.mapped(JsonElement::getAsJsonArray)
    }

    fun JsonArray.objects(): Iterable<JsonObject> {
        return this.mapped(JsonElement::getAsJsonObject)
    }

    fun <T> JsonArray.mapped(mapper: JsonElement.() -> T): Iterable<T> {
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