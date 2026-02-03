/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.virtual.entity.ParentVirtualEntity
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface allows for the attaching of [VirtualEntity]s.
 *
 * Virtual entities are attached to a given [anchor],
 * and attachments are responsible for ticking the virtual
 * entities and tracking observers.
 *
 * @see RootVirtualEntityAttachment
 * @see ParentVirtualEntity
 */
public interface VirtualEntityAttachment {
    /**
     * Whether the attached should be searched for
     * interaction handlers or not.
     */
    public val interactable: Boolean
        get() = true

    /**
     * The anchor for this attachment.
     */
    public val anchor: AttachmentAnchor

    /**
     * The observer tracker for this attachment.
     */
    @get:OverrideOnly
    public val observers: ObserverTracker

    /**
     * Attaches a given [entity] to this attachment.
     *
     * @param entity The entity to attach.
     * @return Whether the entity was attached successfully.
     */
    public fun attach(entity: VirtualEntity): Boolean

    /**
     * Detaches a given [entity] from this attachment.
     *
     * @param entity The entity to detach.
     * @return Whether the entity was detached successfully.
     */
    public fun detach(entity: VirtualEntity): Boolean

    /**
     * Gets a iterable of all the attached virtual
     * entities.
     *
     * @return The attached virtual entities.
     */
    public fun attached(): Iterable<VirtualEntity>

    /**
     * Ticks the virtual entity attachment.
     */
    public fun tick() {
        for (entity in this.attached()) {
            entity.tick()
        }
    }
}