/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.display

import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.display.SimpleVirtualTextDisplay
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.minecraft.network.chat.Component

public class DynamicVirtualTextDisplay(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker,
    private val text: PlayerSpecificElement<Component>
): SimpleVirtualTextDisplay(attachment, observers) {
    override fun tick() {
        for (observer in this.observers) {
            val player = observer.asPlayerOrNull() ?: continue
            val updated = this.text.get(player)
            this.setTextFor(player, updated)
        }
        super.tick()
    }
}