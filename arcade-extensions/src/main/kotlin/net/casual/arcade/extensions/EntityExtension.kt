/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.all
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import org.jetbrains.annotations.ApiStatus.Internal

public abstract class EntityExtension private constructor(
    private val provider: () -> Entity
): TransferableEntityExtension {
    public val entity: Entity
        get() = this.provider.invoke()

    public constructor(entity: Entity): this(provider(entity))

    public companion object {
        @Internal
        @JvmField
        public val SHOULD_ATTACH_EXTENSION: ThreadLocal<Boolean> = ThreadLocal.withInitial { true }

        private fun provider(entity: Entity): () -> Entity {
            if (entity is ServerPlayer) {
                val connection = entity.connection
                return { connection.player }
            }
            return { entity }
        }
    }
}