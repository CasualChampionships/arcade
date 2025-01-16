/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.minecraft.world.entity.Entity

public data class EntityTickEvent(
    override val entity: Entity
): EntityEvent