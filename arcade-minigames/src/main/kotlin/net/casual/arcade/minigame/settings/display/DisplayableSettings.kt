/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings.display

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.guis.core.container.ContainerGui
import net.casual.arcade.minigame.settings.GameSetting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

public open class DisplayableSettings(public val title: Component) {
    private val displays = Object2ObjectLinkedOpenHashMap<String, MenuGameSetting<*>>()

    /**
     * This registers a setting to this collection.
     *
     * @param display The displayable setting.
     * @return The created [GameSetting].
     */
    public fun <T: Any> register(display: MenuGameSetting<T>): GameSetting<T> {
        val setting = display.setting
        this.displays[setting.name] = display
        return setting
    }

    /**
     * This registers a setting to this collection.
     *
     * @param display The displayable settings builder.
     * @return The created [GameSetting].
     */
    public fun <T: Any> register(display: MenuGameSettingBuilder<T>): GameSetting<T> {
        return this.register(display.build())
    }

    /**
     * This copies all the settings from another instance
     * of [DisplayableSettings] and registers them here.
     *
     * @param settings The settings to copy from.
     */
    public fun copyFrom(settings: DisplayableSettings) {
        for (setting in settings.displays.values) {
            this.register(setting)
        }
    }

    /**
     * This gets all the registered [GameSetting]s.
     *
     * @return A collection of all the settings.
     */
    public fun all(): Collection<GameSetting<*>> {
        return this.displays.values.map { it.setting }
    }

    /**
     * This gets a setting for a given name.
     *
     * @param name The name of the given setting.
     * @return The setting, may be null if non-existent.
     */
    public fun get(name: String): GameSetting<*>? {
        return this.displays[name]?.setting
    }

    /**
     * This creates a menu which can be displayed to a
     * player to directly interact with the settings.
     *
     * @param player The player being displayed to gui.
     * @return The settings gui.
     */
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

    internal fun displays(): Collection<MenuGameSetting<*>> {
        return this.displays.values
    }

    internal fun display(name: String): MenuGameSetting<*>? {
        return this.displays[name]
    }

    private fun <T: Any> ValueOutput.store(key: String, setting: GameSetting<T>) {
        this.store(key, setting.codec(), setting.get())
    }

    private fun <T: Any> ValueInput.read(key: String, setting: GameSetting<T>) {
        val value = this.read(key, setting.codec()).getOrNull()
        if (value != null) {
            setting.setQuietly(value)
        }
    }
}