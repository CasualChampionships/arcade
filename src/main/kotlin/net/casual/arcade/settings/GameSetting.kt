package net.casual.arcade.settings

import com.google.common.collect.ImmutableMap
import com.google.gson.JsonElement
import kotlin.reflect.KProperty

abstract class GameSetting<T: Any>(
    val name: String,
    private var value: T,
    private val options: Map<String, T>
) {
    private val listeners by lazy { ArrayList<SettingListener<T>>() }

    abstract fun serialise(value: T): JsonElement

    abstract fun deserialise(json: JsonElement): T

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
        return ImmutableMap.copyOf(this.options)
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

    fun serialiseValue(): JsonElement {
        return this.serialise(this.get())
    }

    fun deserialiseAndSet(json: JsonElement) {
        this.set(this.deserialise(json))
    }

    fun deserialiseAndSetQuietly(json: JsonElement) {
        this.setQuietly(this.deserialise(json))
    }

    operator fun getValue(any: Any, property: KProperty<*>): T {
        return this.get()
    }

    operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.set(value)
    }
}