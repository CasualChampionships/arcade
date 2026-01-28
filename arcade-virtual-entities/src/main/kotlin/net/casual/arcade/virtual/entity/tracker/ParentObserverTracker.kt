/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.tracker

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

public class ParentObserverTracker(
    private val parent: ObserverTracker
): ObserverTracker {
    override fun startObserving(observer: ServerPlayer): Boolean {
        return true
    }

    override fun stopObserving(observer: ServerPlayer) {

    }

    override fun isObserving(observer: ServerPlayer): Boolean {
        return this.parent.isObserving(observer)
    }

    override fun connections(): Collection<ServerGamePacketListenerImpl> {
        return this.parent.connections()
    }
}