/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.minecraft.server.level.ServerPlayer
import java.util.*
import kotlin.reflect.KProperty

public class GameSetting<T: Any>(
    public val name: String,
    private var value: T,
    private val options: Map<String, T>,
    private val serializer: Codec<T>
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
        val previous = this.get()
        for (listener in this.listeners) {
            listener.onSet(this, previous, value)
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
        return this.serializer.encodeStart(JsonOps.INSTANCE, this.get()).orThrow
    }

    public fun deserializeAndSet(json: JsonElement): Boolean {
        val result = this.serializer.parse(JsonOps.INSTANCE, json).result()
        if (result.isPresent) {
            this.set(result.get())
            return true
        }
        return false
    }

    public fun deserializeAndSetQuietly(json: JsonElement): Boolean {
        val result = this.serializer.parse(JsonOps.INSTANCE, json).result()
        if (result.isPresent) {
            this.setQuietly(result.get())
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
        public fun <T: Any> generator(serializer: Codec<T>): (String, T, Map<String, T>) -> GameSetting<T> {
            return { id, value, options -> GameSetting(id, value, options, serializer) }
        }
    }
}