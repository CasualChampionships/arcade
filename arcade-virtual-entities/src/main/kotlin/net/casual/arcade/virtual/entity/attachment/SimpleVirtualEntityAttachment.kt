/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.attachment

import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor

public open class SimpleVirtualEntityAttachment(
    override val anchor: AttachmentAnchor
): TrackingVirtualEntityAttachment()