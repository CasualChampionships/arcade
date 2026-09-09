/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.entity.EntityEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.casual.arcade.extensions.Extension
import net.minecraft.world.entity.Entity
import net.casual.arcade.extensions.utils.addExtension as addExtensionNew
import net.casual.arcade.extensions.utils.getExtension as getExtensionNew

// This may be broadcasted off-thread, as a result of world-gen
public class EntityExtensionEvent(
    override val entity: Entity
): EntityEvent, ExtensionEvent, AsyncEvent {
    override fun addExtension(extension: Extension) {
        this.entity.addExtensionNew(extension)
    }

    public fun addExtension(provider: (Entity) -> Extension) {
        this.addExtension(provider.invoke(this.entity))
    }
}