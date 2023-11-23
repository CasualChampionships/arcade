package net.casual.arcade.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.arrayOrNull
import net.casual.arcade.utils.JsonUtils.boolean
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrNull
import net.casual.arcade.utils.JsonUtils.booleanOrPut
import net.casual.arcade.utils.JsonUtils.double
import net.casual.arcade.utils.JsonUtils.doubleOrDefault
import net.casual.arcade.utils.JsonUtils.doubleOrNull
import net.casual.arcade.utils.JsonUtils.doubleOrPut
import net.casual.arcade.utils.JsonUtils.float
import net.casual.arcade.utils.JsonUtils.floatOrDefault
import net.casual.arcade.utils.JsonUtils.floatOrNull
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.intOrDefault
import net.casual.arcade.utils.JsonUtils.intOrNull
import net.casual.arcade.utils.JsonUtils.intOrPut
import net.casual.arcade.utils.JsonUtils.number
import net.casual.arcade.utils.JsonUtils.numberOrDefault
import net.casual.arcade.utils.JsonUtils.numberOrNull
import net.casual.arcade.utils.JsonUtils.numberOrPut
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.objOrDefault
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrDefault
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.casual.arcade.utils.JsonUtils.stringOrPut
import java.nio.file.Path
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

    public fun boolean(name: String? = null, default: Boolean = false): Configurable<Boolean> {
        return object: Configurable<Boolean> {
            override fun setValue(any: Any, property: KProperty<*>, value: Boolean) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Boolean {
                return json.booleanOrPut(name ?: property.name) { default }
            }
        }
    }

    public fun string(name: String? = null, default: String = ""): Configurable<String> {
        return object: Configurable<String> {
            override fun setValue(any: Any, property: KProperty<*>, value: String) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): String {
                return json.stringOrPut(name ?: property.name) { default }
            }
        }
    }

    public fun number(name: String? = null, default: Number = 0): Configurable<Number> {
        return object: Configurable<Number> {
            override fun setValue(any: Any, property: KProperty<*>, value: Number) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Number {
                return json.numberOrPut(name ?: property.name) { default }
            }
        }
    }

    public fun int(name: String? = null, default: Int = 0): Configurable<Int> {
        return object: Configurable<Int> {
            override fun setValue(any: Any, property: KProperty<*>, value: Int) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Int {
                return json.intOrPut(name ?: property.name) { default }
            }
        }
    }

    public fun double(name: String? = null, default: Double = 0.0): Configurable<Double> {
        return object: Configurable<Double> {
            override fun setValue(any: Any, property: KProperty<*>, value: Double) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Double {
                return json.doubleOrPut(name ?: property.name) { default }
            }
        }
    }

    public fun booleanOrNull(name: String? = null): Configurable<Boolean?> {
        return object: Configurable<Boolean?> {
            override fun setValue(any: Any, property: KProperty<*>, value: Boolean?) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Boolean? {
                val key = name ?: property.name
                return json.booleanOrNull(key) ?: json.add(key, null).let { null }
            }
        }
    }

    public fun stringOrNull(name: String? = null): Configurable<String?> {
        return object: Configurable<String?> {
            override fun setValue(any: Any, property: KProperty<*>, value: String?) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): String? {
                val key = name ?: property.name
                return json.stringOrNull(key) ?: json.add(key, null).let { null }
            }
        }
    }

    public fun numberOrNull(name: String? = null): Configurable<Number?> {
        return object: Configurable<Number?> {
            override fun setValue(any: Any, property: KProperty<*>, value: Number?) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Number? {
                val key = name ?: property.name
                return json.numberOrNull(key) ?: json.add(key, null).let { null }
            }
        }
    }

    public fun intOrNull(name: String? = null): Configurable<Int?> {
        return object: Configurable<Int?> {
            override fun setValue(any: Any, property: KProperty<*>, value: Int?) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Int? {
                val key = name ?: property.name
                return json.intOrNull(key) ?: json.add(key, null).let { null }
            }
        }
    }

    public fun doubleOrNull(name: String? = null): Configurable<Double?> {
        return object: Configurable<Double?> {
            override fun setValue(any: Any, property: KProperty<*>, value: Double?) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Double? {
                val key = name ?: property.name
                return json.doubleOrNull(key) ?: json.add(key, null).let { null }
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

    public companion object {
        @Deprecated("Use 'JsonUtils.GSON' instead")
        public val GSON: Gson = GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create()
    }
}