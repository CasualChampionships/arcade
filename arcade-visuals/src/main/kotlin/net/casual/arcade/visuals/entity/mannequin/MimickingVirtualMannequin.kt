/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.mannequin

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.packet.PacketSender
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.utils.player.StaticResolvableProfile
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.mannequin.SimpleVirtualMannequin

public class MimickingVirtualMannequin(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker = SimpleObserverTracker()
): SimpleVirtualMannequin(attachment, observers) {
    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        val player = observer.asPlayerOrNull()
        if (player != null) {
            this.setProfileFor(player, StaticResolvableProfile(player.gameProfile))
        }
        super.sendSpawnPackets(observer, sender)
    }
}