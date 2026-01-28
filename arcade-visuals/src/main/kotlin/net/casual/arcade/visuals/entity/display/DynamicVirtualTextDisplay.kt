/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.display

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.display.SimpleVirtualTextDisplay
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.minecraft.network.chat.Component

public class DynamicVirtualTextDisplay(
    private val text: PlayerSpecificElement<Component>,
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker
): SimpleVirtualTextDisplay(attachment, observers) {
    override fun tick() {
        for (observer in this.observers) {
            val updated = this.text.get(observer)
            this.setTextFor(observer, updated)
        }
        super.tick()
    }
}