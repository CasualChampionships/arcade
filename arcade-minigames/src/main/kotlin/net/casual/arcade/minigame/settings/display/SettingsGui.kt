/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings.display

import net.casual.arcade.guis.core.container.ContainerGui
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.utils.ItemUtils.lore
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.component.gray
import net.casual.arcade.utils.component.green
import net.casual.arcade.utils.component.italicize
import net.casual.arcade.utils.component.unitalicize
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

internal class SettingsGui(
    player: ServerPlayer,
    private val settings: DisplayableSettings
): ContainerGui(player, ContainerType.Generic9x6, false) {
    private val displays = this.settings.displays().toList()

    private var page = 0

    init {
        this.setTitle(this.settings.title)
    }

    override fun onOpen() {
        this.loadFooter()
        this.loadPage()
    }

    private fun pages(): Int {
        return ((this.displays.size + PAGE_SIZE - 1) / PAGE_SIZE).coerceAtLeast(1)
    }

    private fun loadPage() {
        val start = this.page * PAGE_SIZE
        for (slot in 0..<PAGE_SIZE) {
            val setting = this.displays.getOrNull(start + slot)
            if (setting == null) {
                this.clearSlot(slot)
                continue
            }
            this.setSlot(slot, this.createSetting(setting)) { action ->
                if (action.isMouse && action != SlotClickAction.MouseDoubleClick) {
                    setting.cycle(if (action.isRight) -1 else 1)
                    this.loadPage()
                }
            }
        }
        this.loadPageControls()
    }

    private fun loadFooter() {
        for (slot in PAGE_SIZE..<this.getContainerSize()) {
            this.setSlot(slot, filler())
        }

        val key = if (this.getParent() == null) "minigame.gui.settings.close" else "minigame.gui.settings.back"
        this.setSlot(CLOSE_SLOT, Items.BARRIER.named(Component.translatable(key))) {
            this.openParentOrClose()
        }
    }

    private fun loadPageControls() {
        val pages = this.pages()
        if (pages <= 1) {
            return
        }
        val previous = this.page > 0
        val next = this.page < pages - 1
        this.setSlot(PREVIOUS_SLOT, this.createPageControl(previous, "minigame.gui.settings.previous")) {
            if (previous) {
                this.page--
                this.loadPage()
            }
        }
        this.setSlot(NEXT_SLOT, this.createPageControl(next, "minigame.gui.settings.next")) {
            if (next) {
                this.page++
                this.loadPage()
            }
        }
    }

    private fun createPageControl(enabled: Boolean, key: String): ItemStack {
        val item = if (enabled) Items.ARROW else Items.DYE.gray
        return item.named(Component.translatable(key, this.page + 1, this.pages()))
    }

    private fun createSetting(setting: MenuGameSetting<*>): ItemStack {
        val stack = setting.display.copy()

        val lore = ArrayList<Component>()
        lore.addAll(stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines())
        lore.add(Component.empty())

        val selected = setting.selected()
        for (option in setting.options) {
            lore.add(this.createOptionLine(option.name, option == selected))
        }
        if (selected == null) {
            val value = Component.literal(setting.value().toString()).italicize()
            lore.add(this.createOptionLine(value, true))
        }
        stack.lore(lore)
        return stack
    }

    private fun createOptionLine(name: Component, selected: Boolean): Component {
        if (selected) {
            return Component.literal(SELECTED_PREFIX).append(name).green().unitalicize()
        }
        return Component.literal(UNSELECTED_PREFIX).append(name).gray().unitalicize()
    }

    companion object {
        private const val PAGE_SIZE = 45

        private const val PREVIOUS_SLOT = 45
        private const val CLOSE_SLOT = 49
        private const val NEXT_SLOT = 53

        private const val SELECTED_PREFIX = "▶ "
        private const val UNSELECTED_PREFIX = "   "

        private fun filler(): ItemStack {
            return Items.STAINED_GLASS_PANE.gray.named(Component.empty())
        }
    }
}
