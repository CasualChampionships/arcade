/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import kotlin.reflect.KProperty

public class GameSetting<T: Any> internal constructor(
    public val name: String,
    public val type: GameSettingType<T>,
    public val options: List<Option<T>>,
    private val display: ItemStack?,
    private val overrides: List<(ServerPlayer) -> T?>,
    listeners: List<SettingListener<T>>,
    appliers: List<SettingApplier<T>>,
    private var value: T
) {
    private val listeners = ArrayList(listeners)
    private val appliers = ArrayList(appliers)

    public fun get(): T {
        return this.value
    }

    public fun get(player: ServerPlayer?): T {
        if (player == null) {
            return this.value
        }
        for (override in this.overrides) {
            val overridden = override.invoke(player)
            if (overridden != null) {
                return overridden
            }
        }
        return this.value
    }

    public fun set(value: T) {
        val previous = this.value
        if (previous == value) {
            return
        }
        this.value = value
        for (listener in this.listeners) {
            listener.onChange(this, previous, value)
        }
        this.apply()
    }

    public fun setQuietly(value: T) {
        this.value = value
    }

    public fun apply() {
        for (applier in this.appliers) {
            applier.onApply(this, this.value)
        }
    }

    public fun option(id: String): Option<T>? {
        return this.options.firstOrNull { it.id == id }
    }

    public fun selected(): Option<T>? {
        return this.options.firstOrNull { it.value == this.value }
    }

    public fun setFromOption(id: String): Boolean {
        val option = this.option(id) ?: return false
        this.set(option.value)
        return true
    }

    public fun cycle(offset: Int) {
        if (this.options.isEmpty()) {
            return
        }
        val index = this.options.indexOfFirst { it.value == this.value }
        val next = if (index == -1) 0 else (index + offset).mod(this.options.size)
        this.set(this.options[next].value)
    }

    public fun hasDisplay(): Boolean {
        return this.display != null
    }

    public fun display(): ItemStack? {
        return this.display?.copy()
    }

    public fun addListener(listener: SettingListener<T>) {
        this.listeners.add(listener)
    }

    public fun addApplier(applier: SettingApplier<T>) {
        this.appliers.add(applier)
    }

    public operator fun getValue(any: Any, property: KProperty<*>): T {
        return this.get()
    }

    public operator fun setValue(any: Any, property: KProperty<*>, value: T) {
        this.set(value)
    }

    override fun toString(): String {
        return "GameSetting[name=${this.name}, value=${this.value}]"
    }

    public data class Option<T: Any>(
        public val id: String,
        public val name: Component,
        public val value: T
    )
}