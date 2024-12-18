/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.utils

import eu.pb4.sgui.api.elements.GuiElement
import eu.pb4.sgui.api.gui.GuiInterface
import eu.pb4.sgui.api.gui.SimpleGui
import net.casual.arcade.minigame.settings.display.DisplayableSettings
import net.casual.arcade.minigame.settings.display.MenuGameSetting
import net.casual.arcade.visuals.screen.SelectionGuiBuilder

public object SettingsGuiUtils {
    public fun SelectionGuiBuilder.addSettings(
        displays: DisplayableSettings,
        generator: (GuiInterface, MenuGameSetting<*>) -> SelectionGuiBuilder = { gui, _ -> SelectionGuiBuilder(gui) }
    ): SelectionGuiBuilder {
        val settings = displays.displays().toList()
        this.elements(settings.indices, { settings[it].display }) { _, _, _, gui, index ->
            createSettingsGui(gui, settings, index, generator).open()
        }
        return this
    }

    public fun <T: Any> SelectionGuiBuilder.addSettingOptions(
        setting: MenuGameSetting<T>
    ): SelectionGuiBuilder {
        for (option in setting.options) {
            this.element(option)
        }
        return this
    }

    private fun createSettingsGui(
        root: GuiInterface,
        settings: List<MenuGameSetting<*>>,
        index: Int,
        generator: (GuiInterface, MenuGameSetting<*>) -> SelectionGuiBuilder
    ): SimpleGui {
        val setting = settings[index]
        val builder = generator.invoke(root, setting)
        builder.addSettingOptions(setting)

        val previous = settings.getOrNull(index - 1)
        if (previous != null) {
            builder.menuElement(SelectionGuiBuilder.MenuSlot.FIRST, GuiElement(previous.display) { _, _, _, _ ->
                this.createSettingsGui(root, settings, index - 1, generator).open()
            })
        }
        val next = settings.getOrNull(index + 1)
        if (next != null) {
            builder.menuElement(SelectionGuiBuilder.MenuSlot.SIXTH, GuiElement(next.display) { _, _, _, _ ->
                this.createSettingsGui(root, settings, index + 1, generator).open()
            })
        }
        return builder.build()
    }
}