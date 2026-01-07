/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.world.entity.Entity
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * An abstract class for all [Entity] extensions.
 *
 * It is recommended that all extensions that are
 * added to entities inherit this class as it implements
 * the [TransferableEntityExtension] and also provides
 * compatibility for certain mods.
 *
 * @param entity The entity that the extension is attached to.
 * @see TransferableEntityExtension
 */
public abstract class EntityExtension(
    public val entity: Entity
): TransferableEntityExtension {
    public companion object {
        @Internal
        @JvmField
        public val SHOULD_ATTACH_EXTENSION: ThreadLocal<Boolean> = ThreadLocal.withInitial { true }
    }
}