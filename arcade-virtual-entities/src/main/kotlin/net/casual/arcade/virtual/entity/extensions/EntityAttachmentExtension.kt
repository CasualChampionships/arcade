/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.entity.EntityTickEvent
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.impl.DelayedActions
import net.casual.arcade.virtual.entity.attachment.EntityVirtualEntityAttachment
import net.minecraft.world.entity.Entity

internal class EntityAttachmentExtension(entity: Entity): EntityExtension(entity) {
    val attachment = EntityVirtualEntityAttachment(entity)

    override fun transfer(
        entity: Entity,
        reason: EntityTransferReason,
        delayed: DelayedActions
    ): Extension {
        return EntityAttachmentExtension(entity)
    }

    companion object {
        val Entity.attachmentExtension: EntityAttachmentExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<EntityExtensionEvent> {
                it.addExtension(::EntityAttachmentExtension)
            }
            GlobalEventHandler.Server.register<EntityTickEvent> { (entity) ->
                entity.attachmentExtension.
            }
        }
    }
}