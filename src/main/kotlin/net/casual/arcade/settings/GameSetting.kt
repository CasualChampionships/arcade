package net.casual.arcade.settings

import com.google.gson.JsonElement
import net.casual.arcade.utils.json.JsonSerializer
import java.util.*
import kotlin.reflect.KProperty

public open class GameSetting<T: Any>(
    public val name: String,
    private var value: T,
    private val options: Map<String, T>,
    private val serializer: JsonSerializer<T>
) {
    private val listeners by lazy { ArrayList<SettingListener<T>>() }

    public fun get(): T {
        return this.value
    }

    public fun set(value: T) {
        for (listener in this.listeners) {
            listener.onSet(this, value)
        }
        this.setQuietly(value)
    }

    public fun setQuietly(value: T) {
        this.value = value
    }

    public fun getOptions(): Map<String, T> {
        return Collections.unmodifiableMap(this.options)
    }

    public fun getOption(option: String): T? {
        return this.options[option]
    }

    public fun setFromOption(option: String) {
        val value = this.getOption(option)
        if (value != null) {
            this.set(value)
        }
    }

    public fun addListener(listener: SettingListener<T>) {
        this.listeners.add(listener)
    }

    public fun serializeValue(): JsonElement {
        return this.serializer.serialize(this.get())
    }

    public fun deserializeAndSet(json: JsonElement) {
        this.set(this.serializer.deserialize(json))
    }

    public fun deserializeAndSetQuietly(json: JsonElement) {
        this.setQuietly(this.serializer.deserialize(json))
    }

    public operator fun getValue(any: Any, property: KProperty<*>): T {
        return this.get()
    }

    public operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.set(value)
    }
}