package net.casualuhc.arcade.settings

import com.google.gson.JsonElement
import kotlin.reflect.KProperty

abstract class GameSetting<T: Any>(
    val name: String,
    private var value: T,
    private val options: Map<String, T>
) {
    private val listeners by lazy { ArrayList<SettingListener<T>>() }

    abstract fun serialise(): JsonElement

    abstract fun deserialise(json: JsonElement)

    fun get(): T {
        return this.value
    }

    fun set(value: T) {
        for (listener in this.listeners) {
            listener.onSet(this, value)
        }
        this.setQuietly(value)
    }

    fun setQuietly(value: T) {
        if (this.value != value) {
            this.value = value
        }
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

    operator fun getValue(any: Any, property: KProperty<*>): T {
        return this.get()
    }

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.set(value)
    }
}