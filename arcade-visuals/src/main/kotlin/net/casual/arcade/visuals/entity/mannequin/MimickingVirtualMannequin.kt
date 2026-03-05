/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.mannequin

import net.casual.arcade.utils.player.StaticResolvableProfile
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.mannequin.SimpleVirtualMannequin
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.tracker.SimpleObserverTracker
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

public class MimickingVirtualMannequin(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker = SimpleObserverTracker()
): SimpleVirtualMannequin(attachment, observers) {
    override fun sendSpawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        this.setProfileFor(observer, StaticResolvableProfile(observer.gameProfile))
        super.sendSpawnPackets(observer, consumer)
    }
}