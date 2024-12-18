/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings.display

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import eu.pb4.sgui.api.gui.GuiInterface
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.minigame.utils.SettingsGuiUtils.addSettings
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.minecraft.server.level.ServerPlayer

public open class DisplayableSettings(
    protected val defaults: DisplayableSettingsDefaults
) {
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
     * @return The gui interface.
     */
    public fun gui(player: ServerPlayer): GuiInterface {
        val builder = this.defaults.createSettingsGuiBuilder(player)
        builder.addSettings(this, this.defaults::createOptionsGuiBuilder)
        return builder.build()
    }

    /**
     * This creates a menu which can be displayed to a
     * player to directly interact with the settings.
     *
     * @param parent The parent ui.
     * @return The gui interface.
     */
    public fun gui(parent: GuiInterface): GuiInterface {
        val builder = this.defaults.createSettingsGuiBuilder(parent.player)
        builder.addSettings(this, this.defaults::createOptionsGuiBuilder)
        return builder.parent(parent).build()
    }

    public fun serialize(): JsonArray {
        val settings = JsonArray()
        for (setting in this.all()) {
            val data = JsonObject()
            data.addProperty("name", setting.name)
            data.add("value", setting.serializeValue())
            settings.add(data)
        }
        return settings
    }

    public fun deserialize(settings: JsonArray) {
        for (data in settings.objects()) {
            val name = data.string("name")
            val value = data.get("value")
            val setting = this.get(name)
            if (setting == null || !setting.deserializeAndSetQuietly(value)) {
                continue
            }
        }
    }

    internal fun displays(): Collection<MenuGameSetting<*>> {
        return this.displays.values
    }
}