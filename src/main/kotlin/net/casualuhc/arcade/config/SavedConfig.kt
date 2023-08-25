package net.casualuhc.arcade.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casualuhc.arcade.utils.JsonUtils.array
import net.casualuhc.arcade.utils.JsonUtils.arrayOrDefault
import net.casualuhc.arcade.utils.JsonUtils.arrayOrNull
import net.casualuhc.arcade.utils.JsonUtils.boolean
import net.casualuhc.arcade.utils.JsonUtils.booleanOrDefault
import net.casualuhc.arcade.utils.JsonUtils.booleanOrNull
import net.casualuhc.arcade.utils.JsonUtils.double
import net.casualuhc.arcade.utils.JsonUtils.doubleOrDefault
import net.casualuhc.arcade.utils.JsonUtils.doubleOrNull
import net.casualuhc.arcade.utils.JsonUtils.float
import net.casualuhc.arcade.utils.JsonUtils.floatOrDefault
import net.casualuhc.arcade.utils.JsonUtils.floatOrNull
import net.casualuhc.arcade.utils.JsonUtils.int
import net.casualuhc.arcade.utils.JsonUtils.intOrDefault
import net.casualuhc.arcade.utils.JsonUtils.intOrNull
import net.casualuhc.arcade.utils.JsonUtils.number
import net.casualuhc.arcade.utils.JsonUtils.numberOrDefault
import net.casualuhc.arcade.utils.JsonUtils.numberOrNull
import net.casualuhc.arcade.utils.JsonUtils.getObject
import net.casualuhc.arcade.utils.JsonUtils.objOrDefault
import net.casualuhc.arcade.utils.JsonUtils.objOrNull
import net.casualuhc.arcade.utils.JsonUtils.string
import net.casualuhc.arcade.utils.JsonUtils.stringOrDefault
import net.casualuhc.arcade.utils.JsonUtils.stringOrNull
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists
import kotlin.reflect.KProperty

open class SavedConfig(
    private val path: Path
) {
    private var json = JsonObject()
    
    fun read() {
        if (this.path.exists()) {
            this.json = GSON.fromJson(this.path.bufferedReader(), JsonObject::class.java)
        }
    }

    fun write() {
        GSON.toJson(this.json, this.path.bufferedWriter())
    }

    fun boolean(name: String? = null, default: Boolean = false): Configurable<Boolean> {
        return object: Configurable<Boolean> {
            override fun setValue(any: Any, property: KProperty<*>, value: Boolean) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Boolean {
                return json.booleanOrDefault(name ?: property.name, default)
            }
        }
    }

    fun string(name: String? = null, default: String = ""): Configurable<String> {
        return object: Configurable<String> {
            override fun setValue(any: Any, property: KProperty<*>, value: String) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): String {
                return json.stringOrDefault(name ?: property.name, default)
            }
        }
    }

    fun number(name: String? = null, default: Number = 0): Configurable<Number> {
        return object: Configurable<Number> {
            override fun setValue(any: Any, property: KProperty<*>, value: Number) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Number {
                return json.numberOrDefault(name ?: property.name, default)
            }
        }
    }

    fun int(name: String? = null, default: Int = 0): Configurable<Int> {
        return object: Configurable<Int> {
            override fun setValue(any: Any, property: KProperty<*>, value: Int) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Int {
                return json.intOrDefault(name ?: property.name, default)
            }
        }
    }

    fun double(name: String? = null, default: Double = 0.0): Configurable<Double> {
        return object: Configurable<Double> {
            override fun setValue(any: Any, property: KProperty<*>, value: Double) {
                json.addProperty(name ?: property.name, value)
            }

            override fun getValue(any: Any, property: KProperty<*>): Double {
                return json.doubleOrDefault(name ?: property.name, default)
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
        return this.json.getObject(key)
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