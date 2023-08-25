package net.casual.arcade.utils

import com.google.gson.JsonArray
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

    fun JsonObject.string(key: String): String {
        return this.get(key).asString
    }

    fun JsonObject.stringOrNull(key: String): String? {
        return this.get(key)?.asString
    }

    fun JsonObject.stringOrDefault(key: String, default: String = ""): String {
        return this.get(key)?.asString ?: default
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

    fun JsonObject.int(key: String): Int {
        return this.get(key).asInt
    }

    fun JsonObject.intOrNull(key: String): Int? {
        return this.get(key)?.asInt
    }

    fun JsonObject.intOrDefault(key: String, default: Int = 0): Int {
        return this.get(key)?.asInt ?: default
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

    fun JsonObject.double(key: String): Double {
        return this.get(key).asDouble
    }

    fun JsonObject.doubleOrNull(key: String): Double? {
        return this.get(key)?.asDouble
    }

    fun JsonObject.doubleOrDefault(key: String, default: Double = 0.0): Double {
        return this.get(key)?.asDouble ?: default
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

    fun JsonObject.getObject(key: String): JsonObject {
        return this.get(key).asJsonObject
    }

    fun JsonObject.objOrNull(key: String): JsonObject? {
        return this.get(key)?.asJsonObject
    }

    fun JsonObject.objOrDefault(key: String, default: JsonObject = JsonObject()): JsonObject {
        return this.get(key)?.asJsonObject ?: default
    }

    fun JsonArray.objects(): Iterable<JsonObject> {
        return object: Iterable<JsonObject> {
            override fun iterator(): Iterator<JsonObject> {
                return object: Iterator<JsonObject> {
                    private var index = 0

                    override fun hasNext(): Boolean {
                        return this.index < size()
                    }

                    override fun next(): JsonObject {
                        return get(this.index++).asJsonObject
                    }
                }
            }
        }
    }
}