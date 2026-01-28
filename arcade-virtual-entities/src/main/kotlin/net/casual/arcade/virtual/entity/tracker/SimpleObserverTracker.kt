/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.tracker

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

public class SimpleObserverTracker: ObserverTracker {
    private val tracking = ReferenceLinkedOpenHashSet<ServerGamePacketListenerImpl>()

    override fun startObserving(observer: ServerPlayer): Boolean {
        return this.tracking.add(observer.connection)
    }

    override fun stopObserving(observer: ServerPlayer) {
        this.tracking.remove(observer.connection)
    }

    override fun isObserving(observer: ServerPlayer): Boolean {
        return this.tracking.contains(observer.connection)
    }

    override fun connections(): Collection<ServerGamePacketListenerImpl> {
        return this.tracking
    }
}