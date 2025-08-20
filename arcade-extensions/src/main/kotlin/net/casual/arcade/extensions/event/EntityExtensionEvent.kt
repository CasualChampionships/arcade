/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.entity.EntityEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.all
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.casual.arcade.extensions.ducks.DebugFlagsHolder
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.world.entity.Entity

// This may be broadcasted off-thread, as a result of world-gen
public class EntityExtensionEvent(
    override val entity: Entity
): EntityEvent, ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.entity.addExtension(extension)
    }

    public fun addExtension(provider: (Entity) -> Extension) {
        this.addExtension(provider.invoke(this.entity))
    }

    public companion object {
        public fun Entity.addExtension(extension: Extension) {
            (this as ExtensionHolder).add(extension)
        }

        public fun <T: Extension> Entity.getExtension(type: Class<T>): T {
            try {
                return (this as ExtensionHolder).get(type)
            } catch (exception: IllegalStateException) {
                val extensions = (this as ExtensionHolder).all()
                val flags = (this as DebugFlagsHolder).`arcade$getFlags`()
                ArcadeUtils.logger.error("Failed to get extension for entity: $this", exception)
                ArcadeUtils.logger.error("Further details:")
                ArcadeUtils.logger.error("  Tick Count: ${this.tickCount}")
                ArcadeUtils.logger.error("  Passengers: ${this.passengers}")
                ArcadeUtils.logger.error("  Extensions: ${extensions.map { it::class.java.simpleName }}")
                ArcadeUtils.logger.error("  Flags: $flags")
                throw exception
            }
        }

        public inline fun <reified T: Extension> Entity.getExtension(): T {
            return this.getExtension(T::class.java)
        }
    }
}