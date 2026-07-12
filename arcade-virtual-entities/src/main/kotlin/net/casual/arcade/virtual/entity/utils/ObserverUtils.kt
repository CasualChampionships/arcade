/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserverExtension
import net.casual.arcade.virtual.entity.observer.Observer
import net.casual.arcade.virtual.entity.observer.PlayerObserver
import net.minecraft.server.level.ServerPlayer

public fun Observer.asPlayerOrNull(): ServerPlayer? {
    return if (this is PlayerObserver) this.player else null
}

public fun ServerPlayer.asObserver(): PlayerObserver {
    return this.attachmentObserverExtension.observer
}