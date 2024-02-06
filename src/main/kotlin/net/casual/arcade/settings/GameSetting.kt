package net.casual.arcade.settings

import com.google.gson.JsonElement
import net.casual.arcade.utils.json.JsonSerializer
import net.minecraft.server.level.ServerPlayer
import java.util.*
import kotlin.reflect.KProperty

public open class GameSetting<T: Any>(
    public val name: String,
    private var value: T,
    private val options: Map<String, T>,
    private val serializer: JsonSerializer<T>
) {
    private val listeners by lazy { ArrayList<SettingListener<T>>() }
    public var override: (ServerPlayer) -> T? = { null }

    public fun get(): T {
        return this.value
    }

    public fun get(player: ServerPlayer): T {
        return this.override(player) ?: this.get()
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

    public fun setFromOption(option: String): Boolean {
        val value = this.getOption(option)
        if (value != null) {
            this.set(value)
            return true
        }
        return false
    }

    public fun addListener(listener: SettingListener<T>) {
        this.listeners.add(listener)
    }

    public fun serializeValue(): JsonElement {
        return this.serializer.serialize(this.get())
    }

    public fun deserializeAndSet(json: JsonElement): Boolean {
        this.runCatching {
            this.set(this.serializer.deserialize(json))
            return true
        }
        return false
    }

    public fun deserializeAndSetQuietly(json: JsonElement): Boolean {
        this.runCatching {
            this.setQuietly(this.serializer.deserialize(json))
            return true
        }
        return false
    }

    public operator fun getValue(any: Any, property: KProperty<*>): T {
        return this.get()
    }

    public operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.set(value)
    }

    public companion object {
        public fun <T: Any> generator(serializer: JsonSerializer<T>): (String, T, Map<String, T>) -> GameSetting<T> {
            return { id, value, options -> GameSetting(id, value, options, serializer) }
        }
    }
}