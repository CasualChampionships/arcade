package net.casual.arcade.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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

open class CustomisableConfig(
    private val path: Path
) {
    private var json = JsonObject()
    
    fun read() {
        if (this.path.exists()) {
            this.path.bufferedReader().use {
                this.json = GSON.fromJson(it, JsonObject::class.java)
            }
        }
    }

    fun write() {
        this.path.bufferedWriter().use {
            GSON.toJson(this.json, it)
        }
    }

    fun boolean(name: String? = null, default: Boolean = false): Configurable<Boolean> {
        return object: Configurable<Boolean> {
            override fun setValue(any: Any, property: KProperty<*>, value: Boolean) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Boolean {
                return json.booleanOrPut(name ?: property.name) { default }
            }
        }
    }

    fun string(name: String? = null, default: String = ""): Configurable<String> {
        return object: Configurable<String> {
            override fun setValue(any: Any, property: KProperty<*>, value: String) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): String {
                return json.stringOrPut(name ?: property.name) { default }
            }
        }
    }

    fun number(name: String? = null, default: Number = 0): Configurable<Number> {
        return object: Configurable<Number> {
            override fun setValue(any: Any, property: KProperty<*>, value: Number) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Number {
                return json.numberOrPut(name ?: property.name) { default }
            }
        }
    }

    fun int(name: String? = null, default: Int = 0): Configurable<Int> {
        return object: Configurable<Int> {
            override fun setValue(any: Any, property: KProperty<*>, value: Int) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Int {
                return json.intOrPut(name ?: property.name) { default }
            }
        }
    }

    fun double(name: String? = null, default: Double = 0.0): Configurable<Double> {
        return object: Configurable<Double> {
            override fun setValue(any: Any, property: KProperty<*>, value: Double) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Double {
                return json.doubleOrPut(name ?: property.name) { default }
            }
        }
    }

    fun booleanOrNull(name: String? = null): Configurable<Boolean?> {
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

    fun stringOrNull(name: String? = null): Configurable<String?> {
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

    fun numberOrNull(name: String? = null): Configurable<Number?> {
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

    fun intOrNull(name: String? = null): Configurable<Int?> {
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

    fun doubleOrNull(name: String? = null): Configurable<Double?> {
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
    
    fun getBoolean(key: String): Boolean {
        return this.json.boolean(key)
    }

    fun getBooleanOrNull(key: String): Boolean? {
        return this.json.booleanOrNull(key)
    }

    fun getBooleanOrDefault(key: String, default: Boolean = false): Boolean {
        return this.json.booleanOrDefault(key, default)
    }

    fun getString(key: String): String {
        return this.json.string(key)
    }

    fun getStringOrNull(key: String): String? {
        return this.json.stringOrNull(key)
    }

    fun getStringOrDefault(key: String, default: String = ""): String {
        return this.json.stringOrDefault(key, default)
    }

    fun getNumber(key: String): Number {
        return this.json.number(key)
    }

    fun getNumberOrNull(key: String): Number? {
        return this.json.numberOrNull(key)
    }

    fun getNumberOrDefault(key: String, default: Number = 0): Number {
        return this.json.numberOrDefault(key, default)
    }

    fun getInt(key: String): Int {
        return this.json.int(key)
    }

    fun getIntOrNull(key: String): Int? {
        return this.json.intOrNull(key)
    }

    fun getIntOrDefault(key: String, default: Int = 0): Int {
        return this.json.intOrDefault(key, default)
    }

    fun getFloat(key: String): Float {
        return this.json.float(key)
    }

    fun getFloatOrNull(key: String): Float? {
        return this.json.floatOrNull(key)
    }

    fun getFloatOrDefault(key: String, default: Float = 0.0F): Float {
        return this.json.floatOrDefault(key, default)
    }

    fun getDouble(key: String): Double {
        return this.json.double(key)
    }

    fun getDoubleOrNull(key: String): Double? {
        return this.json.doubleOrNull(key)
    }

    fun getDoubleOrDefault(key: String, default: Double = 0.0): Double {
        return this.json.doubleOrDefault(key, default)
    }

    fun getArray(key: String): JsonArray {
        return this.json.array(key)
    }

    fun getArrayOrNull(key: String): JsonArray? {
        return this.json.arrayOrNull(key)
    }

    fun getArrayOrDefault(key: String, default: JsonArray = JsonArray()): JsonArray {
        return this.json.arrayOrDefault(key, default)
    }

    fun getObject(key: String): JsonObject {
        return this.json.obj(key)
    }

    fun getObjectOrNull(key: String): JsonObject? {
        return this.json.objOrNull(key)
    }

    fun getObjectOrDefault(key: String, default: JsonObject = JsonObject()): JsonObject {
        return this.json.objOrDefault(key, default)
    }

    companion object {
        val GSON: Gson = GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create()
    }
}