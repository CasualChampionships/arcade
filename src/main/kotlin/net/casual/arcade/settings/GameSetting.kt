package net.casual.arcade.settings

import com.google.gson.JsonElement
import net.casual.arcade.utils.json.JsonSerializer
import java.util.*
import kotlin.reflect.KProperty

open class GameSetting<T: Any>(
    val name: String,
    private var value: T,
    private val options: Map<String, T>,
    private val serializer: JsonSerializer<T>
) {
    private val listeners by lazy { ArrayList<SettingListener<T>>() }

    fun get(): T {
        return this.value
    }

    fun set(value: T) {
        if (this.get() != value) {
            for (listener in this.listeners) {
                listener.onSet(this, value)
            }
            this.setQuietly(value)
        }
    }

    fun setQuietly(value: T) {
        this.value = value
    }

    fun getOptions(): Map<String, T> {
        return Collections.unmodifiableMap(this.options)
    }

    fun getOption(option: String): T? {
        return this.options[option]
    }

    fun setFromOption(option: String) {
        val value = this.getOption(option)
        if (value != null) {
            this.set(value)
        }
    }

    fun addListener(listener: SettingListener<T>) {
        this.listeners.add(listener)
    }

    fun serializeValue(): JsonElement {
        return this.serializer.serialize(this.get())
    }

    fun deserializeAndSet(json: JsonElement) {
        this.set(this.serializer.deserialize(json))
    }

    fun deserializeAndSetQuietly(json: JsonElement) {
        this.setQuietly(this.serializer.deserialize(json))
    }

    operator fun getValue(any: Any, property: KProperty<*>): T {
        return this.get()
    }

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.set(value)
    }
}