/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerSlotClickEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.guis.utils.getOpenGui
import net.casual.arcade.guis.utils.invalidateRemoteSlots

internal object GuiMenuEvents {
    fun registerEvents() {
        GlobalEventHandler.Server.register<PlayerTickEvent>(::onPlayerTick)
        GlobalEventHandler.Server.register<PlayerSlotClickEvent>(
            phase = PlayerSlotClickEvent.PHASE_PRE_VALIDATE, listener = ::onPlayerSlotClick
        )
    }

    private fun onPlayerTick(event: PlayerTickEvent) {
        val menu = event.player.containerMenu
        if (menu is GuiMenu<*>) {
            menu.tick()
        }
    }

    private fun onPlayerSlotClick(event: PlayerSlotClickEvent) {
        val (player, menu, index, button, input, _, stateId, changed, carried) = event
        if (menu is ContainerGuiMenu) {
            val gui = menu.gui
            if (player.isSpectator && !gui.canSpectatorsClick) {
                return
            }
            val action = SlotClickAction.from(input, button, index)

            menu.suppressRemoteUpdates()
            gui.click(index, action)

            for (entry in Int2ObjectMaps.fastIterable(changed)) {
                menu.setRemoteSlotUnsafe(entry.intKey, entry.value)
            }
            menu.setRemoteCarried(carried)
            menu.resumeRemoteUpdates()

            if (stateId != menu.stateId) {
                menu.invalidateRemoteSlots()
            }

            event.cancel()
        }
    }
}