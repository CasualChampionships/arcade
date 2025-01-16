/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.entity.EntityEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.minecraft.world.entity.Entity

public class EntityExtensionEvent(
    override val entity: Entity
): EntityEvent, ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.entity.addExtension(extension);
    }

    public fun addExtension(provider: (Entity) -> Extension) {
        this.addExtension(provider.invoke(this.entity))
    }

    public companion object {
        public fun Entity.addExtension(extension: Extension) {
            (this as ExtensionHolder).add(extension)
        }

        public fun <T: Extension> Entity.getExtension(type: Class<T>): T {
            return (this as ExtensionHolder).get(type)
        }

        public inline fun <reified T: Extension> Entity.getExtension(): T {
            return this.getExtension(T::class.java)
        }
    }
}