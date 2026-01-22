/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor

public interface VirtualEntityAttachment {
    public val anchor: AttachmentAnchor

    public fun attach(entity: VirtualEntity): Boolean

    public fun detach(entity: VirtualEntity): Boolean

    public fun attached(): Collection<VirtualEntity>

    public fun tick() {
        for (entity in this.attached()) {
            entity.tick()
        }
    }
}