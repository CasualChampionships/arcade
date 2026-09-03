/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.guis.core.container.ContainerGui
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

public open class GameSettings(public val title: Component) {
    private val settings = Object2ObjectLinkedOpenHashMap<String, GameSetting<*>>()

    public fun <T: Any> register(setting: GameSetting<T>): GameSetting<T> {
        this.settings[setting.name] = setting
        return setting
    }

    public fun <T: Any> register(builder: GameSettingBuilder<T>): GameSetting<T> {
        return this.register(builder.build())
    }

    public fun copyFrom(settings: GameSettings) {
        for (setting in settings.settings.values) {
            this.register(setting)
        }
    }

    public fun all(): Collection<GameSetting<*>> {
        return this.settings.values
    }

    public fun get(name: String): GameSetting<*>? {
        return this.settings[name]
    }

    public fun gui(player: ServerPlayer): ContainerGui {
        return SettingsGui(player, this)
    }

    public fun serialize(list: ValueOutput.ValueOutputList) {
        for (setting in this.all()) {
            val output = list.addChild()
            output.putString("name", setting.name)
            output.store("value", setting)
        }
    }

    public fun deserialize(list: ValueInput.ValueInputList) {
        for (input in list) {
            val name = input.getString("name").getOrNull() ?: continue
            val setting = this.get(name)
            if (setting != null) {
                input.read("value", setting)
            }
        }
    }

    internal fun initialize() {
        for (setting in this.all()) {
            setting.apply()
        }
    }

    internal fun displayable(): List<GameSetting<*>> {
        return this.all().filter { it.hasDisplay() }
    }

    private fun <T: Any> ValueOutput.store(key: String, setting: GameSetting<T>) {
        this.store(key, setting.type.codec, setting.get())
    }

    private fun <T: Any> ValueInput.read(key: String, setting: GameSetting<T>) {
        val value = this.read(key, setting.type.codec).getOrNull()
        if (value != null) {
            setting.setQuietly(value)
        }
    }
}
