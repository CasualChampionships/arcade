/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.level.LevelTickEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.virtual.entity.attachment.LevelVirtualEntityAttachment
import net.minecraft.server.level.ServerLevel

internal class LevelAttachmentExtension(level: ServerLevel): Extension {
    val attachment = LevelVirtualEntityAttachment(level)

    companion object {
        val ServerLevel.attachmentExtension: LevelAttachmentExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> {
                it.addExtension(::LevelAttachmentExtension)
            }
            GlobalEventHandler.Server.register<LevelTickEvent> { (level) ->
                level.attachmentExtension.attachment.tick()
            }
        }
    }
}