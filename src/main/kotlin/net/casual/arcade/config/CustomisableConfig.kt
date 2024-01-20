package net.casual.arcade.config

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.arrayOrNull
import net.casual.arcade.utils.JsonUtils.boolean
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrNull
import net.casual.arcade.utils.JsonUtils.double
import net.casual.arcade.utils.JsonUtils.doubleOrDefault
import net.casual.arcade.utils.JsonUtils.doubleOrNull
import net.casual.arcade.utils.JsonUtils.float
import net.casual.arcade.utils.JsonUtils.floatOrDefault
import net.casual.arcade.utils.JsonUtils.floatOrNull
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.intOrDefault
import net.casual.arcade.utils.JsonUtils.intOrNull
import net.casual.arcade.utils.JsonUtils.number
import net.casual.arcade.utils.JsonUtils.numberOrDefault
import net.casual.arcade.utils.JsonUtils.numberOrNull
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objOrDefault
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrDefault
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.casual.arcade.utils.JsonUtils.uuidOrNull
import net.casual.arcade.utils.json.*
import net.minecraft.Util
import java.nio.file.Path
import java.util.UUID
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists
import kotlin.reflect.KProperty

public open class CustomisableConfig(
    private val path: Path
) {
    private var json = JsonObject()
    
    public fun read() {
        if (this.path.exists()) {
            this.path.bufferedReader().use {
                this.json = JsonUtils.GSON.fromJson(it, JsonObject::class.java)
            }
        }
    }

    public fun write() {
        this.path.bufferedWriter().use {
            JsonUtils.GSON.toJson(this.json, it)
        }
    }

    public operator fun set(name: String, json: JsonElement) {
        this.json.add(name, json)
    }

    public operator fun set(name: String, none: Nothing?) {
        this.json.add(name, none)
    }

    public operator fun set(name: String, string: String) {
        this.json.addProperty(name, string)
    }

    public operator fun set(name: String, number: Number) {
        this.json.addProperty(name, number)
    }

    public operator fun set(name: String, boolean: Boolean) {
        this.json.addProperty(name, boolean)
    }

    public operator fun set(name: String, char: Char) {
        this.json.addProperty(name, char)
    }

    public fun boolean(name: String? = null, default: Boolean = false): Configurable<Boolean> {
        return any(name, default, BooleanSerializer)
    }

    public fun string(name: String? = null, default: String = ""): Configurable<String> {
        return any(name, default, StringSerializer)
    }

    public fun int(name: String? = null, default: Int = 0): Configurable<Int> {
        return any(name, default, IntSerializer)
    }

    public fun double(name: String? = null, default: Double = 0.0): Configurable<Double> {
        return any(name, default, DoubleSerializer)
    }

    public fun uuid(name: String? = null, default: UUID = Util.NIL_UUID): Configurable<UUID> {
        return any(name, default, UUIDSerializer)
    }

    public fun <T: Any> any(name: String? = null, default: T, serializer: JsonSerializer<T>): Configurable<T> {
        return object: Configurable<T> {
            override fun setValue(any: Any, property: KProperty<*>, value: T) {
                json.add(name ?: property.name, serializer.serialize(value))
            }

            override fun getValue(any: Any, property: KProperty<*>): T {
                val key = name ?: property.name
                val element = json.get(key)
                if (element == null) {
                    json.add(key, serializer.serialize(default))
                    return default
                }
                return serializer.deserialize(element)
            }
        }
    }

    public fun booleanOrNull(name: String? = null): Configurable<Boolean?> {
        return anyOrNull(name, BooleanSerializer)
    }

    public fun stringOrNull(name: String? = null): Configurable<String?> {
        return anyOrNull(name, StringSerializer)
    }

    public fun intOrNull(name: String? = null): Configurable<Int?> {
        return anyOrNull(name, IntSerializer)
    }

    public fun doubleOrNull(name: String? = null): Configurable<Double?> {
        return anyOrNull(name, DoubleSerializer)
    }

    public fun uuidOrNull(name: String? = null): Configurable<UUID?> {
        return anyOrNull(name, UUIDSerializer)
    }

    public fun <T: Any> anyOrNull(name: String? = null, serializer: JsonSerializer<T>): Configurable<T?> {
        return object: Configurable<T?> {
            override fun setValue(any: Any, property: KProperty<*>, value: T?) {
                json.add(name ?: property.name, if (value == null) null else serializer.serialize(value))
            }

            override fun getValue(any: Any, property: KProperty<*>): T? {
                val key = name ?: property.name
                val element = json.get(key)
                if (element == null) {
                    json.add(key, null)
                    return null
                }
                return serializer.deserialize(element)
            }
        }
    }
    
    public fun getBoolean(key: String): Boolean {
        return this.json.boolean(key)
    }

    public fun getBooleanOrNull(key: String): Boolean? {
        return this.json.booleanOrNull(key)
    }

    public fun getBooleanOrDefault(key: String, default: Boolean = false): Boolean {
        return this.json.booleanOrDefault(key, default)
    }

    public fun getString(key: String): String {
        return this.json.string(key)
    }

    public fun getStringOrNull(key: String): String? {
        return this.json.stringOrNull(key)
    }

    public fun getStringOrDefault(key: String, default: String = ""): String {
        return this.json.stringOrDefault(key, default)
    }

    public fun getNumber(key: String): Number {
        return this.json.number(key)
    }

    public fun getNumberOrNull(key: String): Number? {
        return this.json.numberOrNull(key)
    }

    public fun getNumberOrDefault(key: String, default: Number = 0): Number {
        return this.json.numberOrDefault(key, default)
    }

    public fun getInt(key: String): Int {
        return this.json.int(key)
    }

    public fun getIntOrNull(key: String): Int? {
        return this.json.intOrNull(key)
    }

    public fun getIntOrDefault(key: String, default: Int = 0): Int {
        return this.json.intOrDefault(key, default)
    }

    public fun getFloat(key: String): Float {
        return this.json.float(key)
    }

    public fun getFloatOrNull(key: String): Float? {
        return this.json.floatOrNull(key)
    }

    public fun getFloatOrDefault(key: String, default: Float = 0.0F): Float {
        return this.json.floatOrDefault(key, default)
    }

    public fun getDouble(key: String): Double {
        return this.json.double(key)
    }

    public fun getDoubleOrNull(key: String): Double? {
        return this.json.doubleOrNull(key)
    }

    public fun getDoubleOrDefault(key: String, default: Double = 0.0): Double {
        return this.json.doubleOrDefault(key, default)
    }

    public fun getArray(key: String): JsonArray {
        return this.json.array(key)
    }

    public fun getArrayOrNull(key: String): JsonArray? {
        return this.json.arrayOrNull(key)
    }

    public fun getArrayOrDefault(key: String, default: JsonArray = JsonArray()): JsonArray {
        return this.json.arrayOrDefault(key, default)
    }

    public fun getObject(key: String): JsonObject {
        return this.json.obj(key)
    }

    public fun getObjectOrNull(key: String): JsonObject? {
        return this.json.objOrNull(key)
    }

    public fun getObjectOrDefault(key: String, default: JsonObject = JsonObject()): JsonObject {
        return this.json.objOrDefault(key, default)
    }
}