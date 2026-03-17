/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.tracker.ObserverTracker

public object VirtualEntityTrackingUtils {
    public fun VirtualEntityAttachment.attachAndUpdateTracking(
        entity: VirtualEntity,
        observers: ObserverTracker,
        tracked: MutableCollection<VirtualEntity>
    ): Boolean {
        if (entity.canAttachTo(this) && tracked.add(entity)) {
            observers.broadcast { observer, consumer ->
                entity.startObservingAndSendPackets(observer, consumer)
            }
            return true
        }
        return false
    }

    @Suppress("UnusedReceiverParameter")
    public fun VirtualEntityAttachment.detachAndUpdateTracking(
        entity: VirtualEntity,
        observers: ObserverTracker,
        tracked: MutableCollection<VirtualEntity>
    ): Boolean {
        if (tracked.remove(entity)) {
            observers.broadcast { observer, consumer ->
                entity.stopObservingAndSendPackets(observer, consumer)
            }
            return true
        }
        return false
    }

    public fun updateTrackedVirtualEntitiesFor(
        observers: ObserverTracker,
        entities: Iterable<VirtualEntity>
    ) {
        observers.broadcast { observer, consumer ->
            for (entity in entities) {
                if (!entity.observers.isObserving(observer)) {
                    entity.startObservingAndSendPackets(observer, consumer)
                } else if (!entity.canObserve(observer)) {
                    entity.stopObservingAndSendPackets(observer, consumer)
                }
            }
        }
    }
}